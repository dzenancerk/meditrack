package com.example.meditrack.workers
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.meditrack.data.notification.*
import com.example.meditrack.data.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
@HiltWorker
class PodsjetnikReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val prefsRepository: UserPreferencesRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val notificationsEnabled = prefsRepository.notificationsEnabled.first()
        if (!notificationsEnabled) return Result.success()
        val podsjetnikId = inputData.getString(KEY_PODSJETNIK_ID) ?: return Result.failure()
        val naziv = inputData.getString(KEY_NAZIV) ?: return Result.failure()
        val tip = inputData.getString(KEY_TIP) ?: return Result.failure()
        val vrijeme = inputData.getString(KEY_VRIJEME) ?: return Result.failure()
        val reminderType = inputData.getString(KEY_REMINDER_TYPE) ?: return Result.failure()
        PodsjetnikNotifier.posaljiNotifikaciju(applicationContext, podsjetnikId, naziv, tip, vrijeme, reminderType)
        return Result.success()
    }
}