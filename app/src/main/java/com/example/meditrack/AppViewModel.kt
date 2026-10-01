package com.example.meditrack
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditrack.data.notification.SnackbarManager
import com.example.meditrack.data.notification.SnackbarMessage
import com.example.meditrack.data.notification.PodsjetnikReminderScheduler
import com.example.meditrack.data.repository.AuthRepository
import com.example.meditrack.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class AppViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val reminderScheduler: PodsjetnikReminderScheduler,
    snackbarManager: SnackbarManager
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = prefsRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val snackbarMessages: SharedFlow<SnackbarMessage> = snackbarManager.messages
    init {
        viewModelScope.launch {
            val vecOdradjeno = prefsRepository.remindersRescheduledOnce.first()
            val ulogovan = prefsRepository.isLoggedIn.first()
            if (!vecOdradjeno && ulogovan) {
                val userId = authRepository.currentUser?.uid
                if (userId != null) {
                    if (reminderScheduler.rescheduleAll(userId)) {
                        prefsRepository.markRemindersRescheduled()
                    }
                }
            }
        }
    }
    fun onNotificationPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            prefsRepository.saveNotificationPermissionGranted(granted)
        }
    }
}