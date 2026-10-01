package com.example.meditrack.data.notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.meditrack.data.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
@AndroidEntryPoint
class PodsjetnikAlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var prefsRepository: UserPreferencesRepository
    override fun onReceive(context: Context, intent: Intent) {
        val podsjetnikId = intent.getStringExtra(KEY_PODSJETNIK_ID) ?: return
        val naziv = intent.getStringExtra(KEY_NAZIV) ?: return
        val tip = intent.getStringExtra(KEY_TIP) ?: return
        val vrijeme = intent.getStringExtra(KEY_VRIJEME) ?: return
        val reminderType = intent.getStringExtra(KEY_REMINDER_TYPE) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (prefsRepository.notificationsEnabled.first()) {
                    PodsjetnikNotifier.posaljiNotifikaciju(context, podsjetnikId, naziv, tip, vrijeme, reminderType)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}