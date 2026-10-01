package com.example.meditrack.data.notification

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SnackbarMessage(
    val text: String,
    val id: Long = System.nanoTime()
)

@Singleton
class SnackbarManager @Inject constructor() {

    private val _messages = MutableSharedFlow<SnackbarMessage>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val messages: SharedFlow<SnackbarMessage> = _messages

    suspend fun show(text: String) {
        _messages.emit(SnackbarMessage(text = text))
    }
}