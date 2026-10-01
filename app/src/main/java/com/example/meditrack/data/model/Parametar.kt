package com.example.meditrack.data.model

import com.google.firebase.firestore.DocumentId

data class Parametar(
    @DocumentId val id: String = "",
    val tip: String = "",
    val vrijednost: Double = 0.0,
    val jedinica: String = "",
    val datum: String = "",
    val vrijeme: String = "",
    val korisnikId: String = ""
)