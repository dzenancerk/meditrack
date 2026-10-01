package com.example.meditrack.data.model

import com.google.firebase.firestore.DocumentId

data class Nalaz(
    @DocumentId val id: String = "",
    val naziv: String = "",
    val tip: String = "",
    val datum: String = "",
    val opis: String = "",
    val prepoznatiTekst: String = "",
    val fileUrls: List<String> = emptyList(),
    val korisnikId: String = ""
){
    companion object {
        const val MAX_SLIKA = 10
    }
}