package com.example.meditrack.ui.theme.signup

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
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val userRepository: UserRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val reminderScheduler: PodsjetnikReminderScheduler
) : ViewModel() {

    var ime by mutableStateOf("")
        private set
    var prezime by mutableStateOf("")
        private set
    var korisnickoIme by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var brojTelefona by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onImeChange(newIme: String) { ime = newIme; errorMessage = null }
    fun onPrezimeChange(newPrezime: String) { prezime = newPrezime; errorMessage = null }
    fun onKorisnickoImeChange(newKorisnickoIme: String) { korisnickoIme = newKorisnickoIme; errorMessage = null }
    fun onEmailChange(newEmail: String) { email = newEmail; errorMessage = null }
    fun onBrojTelefonaChange(newBrojTelefona: String) { brojTelefona = newBrojTelefona; errorMessage = null }
    fun onPasswordChange(newPassword: String) { password = newPassword; errorMessage = null }
    fun onConfirmPasswordChange(newConfirmPassword: String) { confirmPassword = newConfirmPassword; errorMessage = null }

    fun getGoogleSignInIntent(): Intent = authRepository.getGoogleSignInIntent()

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
                    val predlozenoIme = account.displayName
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

    fun onSignUp(onSuccess: () -> Unit) {
        if (!validateInputs()) return
        viewModelScope.launch {
            if (!connectivityObserver.isCurrentlyOnline()) {
                errorMessage = "Nemate internet konekciju. Registracija zahtijeva aktivnu vezu."
                return@launch
            }
            isLoading = true
            errorMessage = null
            try {
                if (userRepository.isKorisnickoImeZauzeto(korisnickoIme)) {
                    errorMessage = "Korisničko ime je već zauzeto"
                    isLoading = false
                    return@launch
                }
                authRepository.signUp(email, password)
                val userId = authRepository.currentUser?.uid
                    ?: throw Exception("Greška pri dobijanju korisničkog ID-a")
                val user = User(
                    id = userId,
                    ime = ime,
                    prezime = prezime,
                    korisnickoIme = korisnickoIme,
                    email = email,
                    brojTelefona = brojTelefona
                )
                userRepository.create(user)
                try {
                    authRepository.sendEmailVerification()
                } catch (e: Exception) {
                }
                prefsRepository.saveLoginState(true)
                prefsRepository.markRemindersRescheduled()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = mapAuthError(e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun validateInputs(): Boolean {
        return when {
            ime.isBlank() -> { errorMessage = "Unesite ime"; false }
            prezime.isBlank() -> { errorMessage = "Unesite prezime"; false }
            korisnickoIme.isBlank() -> { errorMessage = "Unesite korisničko ime"; false }
            email.isBlank() -> { errorMessage = "Unesite email adresu"; false }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> { errorMessage = "Neispravna email adresa"; false }
            brojTelefona.isBlank() -> { errorMessage = "Unesite broj telefona"; false }
            password.isBlank() -> { errorMessage = "Unesite lozinku"; false }
            password.length < 6 -> { errorMessage = "Lozinka mora imati najmanje 6 karaktera"; false }
            confirmPassword.isBlank() -> { errorMessage = "Potvrdite lozinku"; false }
            password != confirmPassword -> { errorMessage = "Lozinke se ne poklapaju"; false }
            else -> true
        }
    }
}