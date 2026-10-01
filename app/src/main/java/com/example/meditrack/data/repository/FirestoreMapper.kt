package com.example.meditrack.data.repository

import com.google.firebase.firestore.FirebaseFirestoreException

fun mapFirestoreError(e: Throwable): String {
    return when (e) {
        is FirebaseFirestoreException -> when (e.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                "Greška sa mrežom. Provjerite internet konekciju."
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "Nemate dozvolu za ovu radnju."
            FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                "Niste prijavljeni. Prijavite se ponovo."
            FirebaseFirestoreException.Code.NOT_FOUND ->
                "Traženi podatak nije pronađen."
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                "Zahtjev je istekao. Pokušajte ponovo."
            FirebaseFirestoreException.Code.ALREADY_EXISTS ->
                "Podatak već postoji."
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED ->
                "Prekoračen je limit zahtjeva. Pokušajte kasnije."
            FirebaseFirestoreException.Code.ABORTED ->
                "Radnja je prekinuta zbog konflikta podataka. Pokušajte ponovo."
            FirebaseFirestoreException.Code.CANCELLED ->
                "Radnja je otkazana."
            else ->
                "Greška pri komunikaciji sa serverom (${e.code})"
        }
        else -> "Greška: ${e.localizedMessage ?: "Nepoznata greška"}"
    }
}