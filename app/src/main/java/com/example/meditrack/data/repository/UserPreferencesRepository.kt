package com.example.meditrack.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

        private val NOTIFICATION_PERMISSION_GRANTED = booleanPreferencesKey("notification_permission_granted")

        private val REMINDERS_RESCHEDULED_ONCE = booleanPreferencesKey("reminders_rescheduled_once")

    }

    suspend fun saveLoginState(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
        }
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    suspend fun clearLoginState() {
        dataStore.edit { preferences ->
            preferences.remove(IS_LOGGED_IN)
            preferences.remove(REMINDERS_RESCHEDULED_ONCE)
        }
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    val notificationPermissionGranted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATION_PERMISSION_GRANTED] ?: true
    }

    suspend fun saveNotificationPermissionGranted(granted: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_PERMISSION_GRANTED] = granted
        }
    }
    val remindersRescheduledOnce: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[REMINDERS_RESCHEDULED_ONCE] ?: false
    }
    suspend fun markRemindersRescheduled() {
        dataStore.edit { preferences ->
            preferences[REMINDERS_RESCHEDULED_ONCE] = true
        }
    }
}