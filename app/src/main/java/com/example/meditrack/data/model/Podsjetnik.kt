package com.example.meditrack.data.model

import com.google.firebase.firestore.DocumentId

data class Podsjetnik(
    @DocumentId val id: String = "",
    val naziv: String = "",
    val tip: String = "",
    val datum: String = "",
    val vrijeme: String = "",
    val opis: String = "",
    val korisnikId: String = ""
)