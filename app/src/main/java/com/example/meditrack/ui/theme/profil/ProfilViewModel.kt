package com.example.meditrack.ui.theme.profil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditrack.data.model.User
import com.example.meditrack.data.repository.AuthRepository
import com.example.meditrack.data.repository.UserPreferencesRepository
import com.example.meditrack.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import com.example.meditrack.data.model.SaveResult
import com.example.meditrack.data.notification.NotificationPermissionChecker
import com.example.meditrack.data.notification.SnackbarManager
import com.example.meditrack.data.repository.mapAuthError
import com.example.meditrack.data.repository.mapFirestoreError
import com.example.meditrack.data.notification.AlarmPermissionChecker
import com.example.meditrack.data.notification.PodsjetnikReminderScheduler
import com.example.meditrack.data.repository.PodsjetniciRepository
import kotlinx.coroutines.flow.first
@HiltViewModel
class ProfilViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val userRepository: UserRepository,
    private val snackbarManager: SnackbarManager,
    private val notificationPermissionChecker: NotificationPermissionChecker,
    private val alarmPermissionChecker: AlarmPermissionChecker,
    private val podsjetniciRepository: PodsjetniciRepository,
    private val reminderScheduler: PodsjetnikReminderScheduler
) : ViewModel() {

    var user by mutableStateOf<User?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var editKorisnickoIme by mutableStateOf("")
        private set
    var editBrojTelefona by mutableStateOf("")
        private set
    var isUpdating by mutableStateOf(false)
        private set
    var updateErrorMessage by mutableStateOf<String?>(null)
        private set
    var notificationsEnabled by mutableStateOf(true)
        private set

    var systemNotificationsBlocked by mutableStateOf(false)
        private set

    var preciznaPodsjetnikaDozvolaBlokirana by mutableStateOf(false)
        private set

    private fun provjeriSistemskuDozvoluNotifikacija() {
        viewModelScope.launch {
            combine(
                prefsRepository.notificationPermissionGranted,
                prefsRepository.notificationsEnabled
            ) { granted, enabled ->
                !granted && enabled
            }.collect { blokirano ->
                systemNotificationsBlocked = blokirano
            }
        }
    }

    fun osvjeziSistemskuDozvoluNotifikacija() {
        viewModelScope.launch {
            prefsRepository.saveNotificationPermissionGranted(
                notificationPermissionChecker.isGranted()
            )
        }
    }

    fun osvjeziAlarmPermission() {
        preciznaPodsjetnikaDozvolaBlokirana = !alarmPermissionChecker.canScheduleExactAlarms()
    }

    init {
        loadUser()
        loadNotificationPreference()
        provjeriVerifikacijuEmaila()
        provjeriSistemskuDozvoluNotifikacija()
        osvjeziAlarmPermission()
    }

    var isEmailVerified by mutableStateOf(true)
        private set
    var isSendingVerification by mutableStateOf(false)
        private set
    var verificationMessage by mutableStateOf<String?>(null)
        private set

    private fun provjeriVerifikacijuEmaila() {
        viewModelScope.launch {
            try {
                authRepository.reloadUser()
            } catch (e: Exception) {
            }
            isEmailVerified = authRepository.isEmailVerified()
        }
    }

    fun posaljiVerifikacioniEmail() {
        viewModelScope.launch {
            isSendingVerification = true
            verificationMessage = null
            try {
                authRepository.sendEmailVerification()
                verificationMessage = "Verifikacioni email je poslan. Provjerite inbox."
            } catch (e: Exception) {
                verificationMessage = mapAuthError(e)
            } finally {
                isSendingVerification = false
            }
        }
    }
    private fun loadUser() {
        viewModelScope.launch {
            isLoading = true
            try {
                val userId = authRepository.currentUser?.uid
                if (userId != null) {
                    user = userRepository.getUser(userId)
                }
            } catch (e: Exception) {
                errorMessage = "Greška pri učitavanju profila: ${mapFirestoreError(e)}"
            } finally {
                isLoading = false
            }
        }
    }

    fun onEditKorisnickoImeChange(value: String) {
        editKorisnickoIme = value
        updateErrorMessage = null
    }

    fun onEditBrojTelefonaChange(value: String) {
        editBrojTelefona = value
        updateErrorMessage = null
    }

    fun prepareEdit() {
        editKorisnickoIme = user?.korisnickoIme ?: ""
        editBrojTelefona = user?.brojTelefona ?: ""
        updateErrorMessage = null
    }

    fun updateUser(onSuccess: () -> Unit) {
        if (editKorisnickoIme.isBlank()) {
            updateErrorMessage = "Unesite korisničko ime"
            return
        }
        if (editBrojTelefona.isBlank()) {
            updateErrorMessage = "Unesite broj telefona"
            return
        }
        val currentUser = user ?: return
        viewModelScope.launch {
            isUpdating = true
            updateErrorMessage = null
            try {
                val userId = authRepository.currentUser?.uid ?: return@launch
                if (userRepository.isKorisnickoImeZauzeto(editKorisnickoIme, korisnikId = userId)) {
                    updateErrorMessage = "Korisničko ime je već zauzeto"
                    return@launch
                }
                val updatedUser = currentUser.copy(
                    korisnickoIme = editKorisnickoIme,
                    brojTelefona = editBrojTelefona
                )
                val rezultat = userRepository.update(updatedUser)
                if (rezultat == SaveResult.TIMED_OUT) {
                    snackbarManager.show("Sačuvano lokalno — sinhronizovaće se automatski kad se veza vrati.")
                }
                user = updatedUser
                onSuccess()
            } catch (e: Exception) {
                updateErrorMessage = "Greška pri izmjeni profila: ${mapFirestoreError(e)}"
            } finally {
                isUpdating = false
            }
        }
    }

    fun onSignOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid
            if (userId != null) {
                try {
                    val podsjetnici = podsjetniciRepository.getPodsjetnici(userId).first()
                    podsjetnici.forEach { podsjetnik ->
                        reminderScheduler.cancel(podsjetnik.id)
                    }
                } catch (e: Exception) {
                }
            }
            authRepository.signOut()
            prefsRepository.clearLoginState()
            onSuccess()
        }
    }

    private fun loadNotificationPreference() {
        viewModelScope.launch {
            prefsRepository.notificationsEnabled.collect { enabled ->
                notificationsEnabled = enabled
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setNotificationsEnabled(enabled)
            notificationsEnabled = enabled
        }
    }
}