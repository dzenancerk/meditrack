package com.example.meditrack.data.model

import android.net.Uri

sealed class NalazSlika {
    data class Postojeca(val url: String) : NalazSlika()
    data class Nova(val uri: Uri) : NalazSlika()
}