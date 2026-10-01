package com.example.meditrack.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

fun mapAuthError(e: Exception): String {
    return when (e) {
        is FirebaseNetworkException ->
            "Greška sa mrežom. Provjerite internet konekciju."
        is FirebaseAuthInvalidUserException ->
            "Korisnik ne postoji"
        is FirebaseAuthUserCollisionException ->
            "Email adresa već postoji"
        is FirebaseAuthWeakPasswordException ->
            "Lozinka je previše slaba"
        is FirebaseAuthInvalidCredentialsException ->
            "Pogrešna email adresa ili lozinka"
        is FirebaseTooManyRequestsException ->
            "Previše pokušaja. Pokušajte kasnije."
        else ->
            "Greška: ${e.localizedMessage ?: "Nepoznata greška"}"
    }
}