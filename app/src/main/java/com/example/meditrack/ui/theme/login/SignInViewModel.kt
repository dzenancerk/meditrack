package com.example.meditrack.ui.theme.login

import android.content.Intent
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditrack.data.model.User
import com.example.meditrack.data.repository.AuthRepository
import com.example.meditrack.data.repository.UserPreferencesRepository
import com.example.meditrack.data.repository.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.meditrack.data.connectivity.ConnectivityObserver
import com.example.meditrack.data.repository.mapAuthError
import com.example.meditrack.data.notification.PodsjetnikReminderScheduler
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val userRepository: UserRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val reminderScheduler: PodsjetnikReminderScheduler
) : ViewModel() {

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newEmail: String) {
        email = newEmail
        errorMessage = null
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        errorMessage = null
    }

    fun getGoogleSignInIntent(): Intent = authRepository.getGoogleSignInIntent()

    var resetEmail by mutableStateOf("")
        private set
    var resetDialogVisible by mutableStateOf(false)
        private set
    var resetMessage by mutableStateOf<String?>(null)
        private set
    var isSendingReset by mutableStateOf(false)
        private set

    fun onResetEmailChange(value: String) {
        resetEmail = value
        resetMessage = null
    }

    fun showResetDialog() {
        resetDialogVisible = true
        resetEmail = email
        resetMessage = null
    }

    fun dismissResetDialog() {
        resetDialogVisible = false
    }

    fun sendPasswordReset() {
        if (resetEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
            resetMessage = "Unesite ispravnu email adresu"
            return
        }
        viewModelScope.launch {
            isSendingReset = true
            resetMessage = null
            try {
                authRepository.sendPasswordResetEmail(resetEmail)
                resetMessage = "Email za reset lozinke je poslan. Provjerite inbox."
            } catch (e: Exception) {
                resetMessage = mapAuthError(e)
            } finally {
                isSendingReset = false
            }
        }
    }

    fun handleGoogleSignInResult(data: Intent?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (!connectivityObserver.isCurrentlyOnline()) {
                errorMessage = "Nemate internet konekciju. Google prijava zahtijeva aktivnu vezu."
                return@launch
            }
            isLoading = true
            errorMessage = null
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                authRepository.signInWithGoogle(account)
                val userId = authRepository.currentUser?.uid
                    ?: throw Exception("Greška pri dobijanju korisničkog ID-a")
                val existingUser = userRepository.getUser(userId)
                if (existingUser == null) {
                    val predlozenoIme = account.displayName?.takeIf { it.isNotBlank() }
                        ?: account.email?.substringBefore("@")
                        ?: "korisnik"
                    val jedinstvenoIme = userRepository.generisiJedinstvenoKorisnickoIme(predlozenoIme)
                    val user = User(
                        id = userId,
                        ime = account.givenName ?: "",
                        prezime = account.familyName ?: "",
                        korisnickoIme = jedinstvenoIme,
                        email = account.email ?: "",
                        brojTelefona = ""
                    )
                    userRepository.create(user)
                }
                prefsRepository.saveLoginState(true)
                if (reminderScheduler.rescheduleAll(userId)) {
                    prefsRepository.markRemindersRescheduled()
                }
                onSuccess()
            } catch (e: Exception) {
                errorMessage = if (e is ApiException) "Google prijava otkazana" else mapAuthError(e)
            } finally {
                isLoading = false
            }
        }
    }

    fun onSignIn(onSuccess: () -> Unit) {
        if (email.isBlank()) {
            errorMessage = "Unesite email adresu"
            return
        }
        if (password.isBlank()) {
            errorMessage = "Unesite lozinku"
            return
        }
        viewModelScope.launch {
            if (!connectivityObserver.isCurrentlyOnline()) {
                errorMessage = "Nemate internet konekciju. Prijava zahtijeva aktivnu vezu."
                return@launch
            }
            isLoading = true
            errorMessage = null
            try {
                authRepository.signIn(email, password)
                prefsRepository.saveLoginState(true)
                val userId = authRepository.currentUser?.uid
                if (userId != null) {
                    if (reminderScheduler.rescheduleAll(userId)) {
                        prefsRepository.markRemindersRescheduled()
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                errorMessage = mapAuthError(e)
            } finally {
                isLoading = false
            }
        }
    }
}