package com.example.meditrack.data.notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.meditrack.data.repository.AuthRepository
import com.example.meditrack.data.repository.PodsjetniciRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    @Inject
    lateinit var authRepository: AuthRepository
    @Inject
    lateinit var podsjetniciRepository: PodsjetniciRepository
    @Inject
    lateinit var reminderScheduler: PodsjetnikReminderScheduler
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = authRepository.currentUser?.uid ?: return@launch
                val podsjetnici = podsjetniciRepository.getPodsjetnici(userId).first()
                podsjetnici.forEach { podsjetnik ->
                    reminderScheduler.zakaziAlarmManagerPodsjetnike(podsjetnik)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}