package com.example.meditrack.data.notification
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.meditrack.data.model.Podsjetnik
import com.example.meditrack.workers.PodsjetnikReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import com.example.meditrack.utils.DateTimeUtils
import androidx.work.ExistingWorkPolicy
import com.example.meditrack.data.repository.PodsjetniciRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class PodsjetnikReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmPermissionChecker: AlarmPermissionChecker,
    private val podsjetniciRepository: PodsjetniciRepository
) {
    companion object {
        private val WORK_MANAGER_TACKE = listOf(
            "24h" to TimeUnit.HOURS.toMillis(24),
            "8h" to TimeUnit.HOURS.toMillis(8)
        )
        private val ALARM_MANAGER_TACKE = listOf(
            "1h" to TimeUnit.HOURS.toMillis(1),
            "15min" to TimeUnit.MINUTES.toMillis(15),
            "5min" to TimeUnit.MINUTES.toMillis(5)
        )
    }
    fun schedule(podsjetnik: Podsjetnik) {
        val podsjetnikDateTime = DateTimeUtils.parseDateTime(podsjetnik.datum, podsjetnik.vrijeme) ?: return
        val now = Date()
        WORK_MANAGER_TACKE.forEach { (label, offset) ->
            val delay = podsjetnikDateTime.time - now.time - offset
            if (delay > 0) zakaziWorkManager(podsjetnik, delay, label)
        }
        zakaziAlarmManagerPodsjetnike(podsjetnik)
    }
    suspend fun rescheduleAll(userId: String): Boolean {
        return try {
            val confirmed = withTimeoutOrNull(30_000L) {
                podsjetniciRepository.getPodsjetnici(userId).first()
                    .forEach { schedule(it) }
                true
            }
            confirmed == true
        } catch (e: Exception) {
            false
        }
    }
    fun zakaziAlarmManagerPodsjetnike(podsjetnik: Podsjetnik) {
        val podsjetnikDateTime = DateTimeUtils.parseDateTime(podsjetnik.datum, podsjetnik.vrijeme) ?: return
        val now = Date()
        val precizniDozvoljeni = alarmPermissionChecker.canScheduleExactAlarms()
        ALARM_MANAGER_TACKE.forEach { (label, offset) ->
            val triggerAt = podsjetnikDateTime.time - offset
            if (triggerAt <= now.time) return@forEach
            if (precizniDozvoljeni) {
                zakaziAlarmManager(podsjetnik, triggerAt, label)
            } else {
                zakaziWorkManager(podsjetnik, triggerAt - now.time, label)
            }
        }
    }
    fun cancel(podsjetnikId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$podsjetnikId")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        ALARM_MANAGER_TACKE.forEach { (label, _) ->
            val pendingIntent = createAlarmPendingIntent(podsjetnikId, label, PendingIntent.FLAG_NO_CREATE)
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
    private fun zakaziWorkManager(podsjetnik: Podsjetnik, delayMillis: Long, reminderType: String) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            "reminder_${podsjetnik.id}_$reminderType",
            ExistingWorkPolicy.REPLACE,
            createReminderWork(podsjetnik, delayMillis, reminderType)
        )
    }
    private fun zakaziAlarmManager(podsjetnik: Podsjetnik, triggerAtMillis: Long, reminderType: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createAlarmPendingIntent(
            podsjetnik.id, reminderType, PendingIntent.FLAG_UPDATE_CURRENT, podsjetnik
        ) ?: return
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }
    private fun requestCodeFor(podsjetnikId: String, reminderType: String): Int {
        val typeOffset = when (reminderType) {
            "1h" -> 1
            "15min" -> 2
            "5min" -> 3
            else -> 0
        }
        return podsjetnikId.hashCode() * 10 + typeOffset
    }
    private fun createAlarmPendingIntent(
        podsjetnikId: String,
        reminderType: String,
        flags: Int,
        podsjetnik: Podsjetnik? = null
    ): PendingIntent? {
        val intent = Intent(context, PodsjetnikAlarmReceiver::class.java).apply {
            putExtra(KEY_PODSJETNIK_ID, podsjetnikId)
            putExtra(KEY_REMINDER_TYPE, reminderType)
            if (podsjetnik != null) {
                putExtra(KEY_NAZIV, podsjetnik.naziv)
                putExtra(KEY_TIP, podsjetnik.tip)
                putExtra(KEY_VRIJEME, podsjetnik.vrijeme)
            }
        }
        val requestCode = requestCodeFor(podsjetnikId, reminderType)
        return PendingIntent.getBroadcast(context, requestCode, intent, flags or PendingIntent.FLAG_IMMUTABLE)
    }
    private fun createReminderWork(podsjetnik: Podsjetnik, delayMillis: Long, reminderType: String) =
        OneTimeWorkRequestBuilder<PodsjetnikReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    KEY_PODSJETNIK_ID to podsjetnik.id,
                    KEY_NAZIV to podsjetnik.naziv,
                    KEY_TIP to podsjetnik.tip,
                    KEY_VRIJEME to podsjetnik.vrijeme,
                    KEY_REMINDER_TYPE to reminderType
                )
            )
            .addTag("reminder_${podsjetnik.id}")
            .build()
}