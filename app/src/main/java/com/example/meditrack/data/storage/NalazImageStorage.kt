package com.example.meditrack.data.storage

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NalazImageStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun copyImageLocally(uri: Uri, userId: String): String? = withContext(Dispatchers.IO) {
        val nalaziDir = File(context.filesDir, "nalazi/$userId").apply { mkdirs() }
        val destFile = File(nalaziDir, "${UUID.randomUUID()}.jpg")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteImages(paths: List<String>) = withContext(Dispatchers.IO) {
        paths.forEach { path ->
            try {
                File(path).delete()
            } catch (e: Exception) {
            }
        }
    }
}