package com.example.meditrack.data.model

import com.google.firebase.firestore.DocumentId

data class KrvniPritisak(
    @DocumentId val id: String = "",
    val sistolicki: Int = 0,
    val dijastolicki: Int = 0,
    val datum: String = "",
    val vrijeme: String = "",
    val korisnikId: String = ""
) {
    companion object {
        const val MIN_RAZMAK_MINUTA = 15
        const val MIN_SISTOLICKI = 40
        const val MAX_SISTOLICKI = 300
        const val MIN_DIJASTOLICKI = 20
        const val MAX_DIJASTOLICKI = 200
        const val GRAF_MIN = 40.0
        const val GRAF_MAX = 200.0
    }
}