package com.example.meditrack.data.notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.meditrack.MainActivity
import com.example.meditrack.R
object PodsjetnikNotifier {
    fun posaljiNotifikaciju(
        context: Context,
        podsjetnikId: String,
        naziv: String,
        tip: String,
        vrijeme: String,
        reminderType: String
    ) {
        val timePrefix = when (reminderType) {
            "24h" -> "Za 24 sata"
            "8h" -> "Za 8 sati"
            "1h" -> "Za 1 sat"
            "15min" -> "Za 15 minuta"
            "5min" -> "Za 5 minuta"
            else -> "Uskoro"
        }
        val message = when (tip) {
            "lijek" -> "$timePrefix imate lijek: $naziv u $vrijeme"
            "pregled" -> "$timePrefix imate pregled: $naziv u $vrijeme"
            else -> "$timePrefix: $naziv u $vrijeme"
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("MediTrack podsjetnik")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        val notificationId = notificationIdFor(podsjetnikId, reminderType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(notificationId, notification)
            }
        } else {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
    fun notificationIdFor(podsjetnikId: String, reminderType: String): Int {
        return (podsjetnikId + "|" + reminderType).hashCode()
    }
}