package com.example.meditrack.data.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.app.NotificationManagerCompat

@Singleton
class NotificationPermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val runtimeDozvoljeno = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!runtimeDozvoljeno) return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}