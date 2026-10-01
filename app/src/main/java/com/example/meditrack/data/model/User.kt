package com.example.meditrack.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val id: String = "",
    val ime: String = "",
    val prezime: String = "",
    val korisnickoIme: String = "",
    val email: String = "",
    val brojTelefona: String = ""
)