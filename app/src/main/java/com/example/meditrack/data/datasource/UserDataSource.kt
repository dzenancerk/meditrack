package com.example.meditrack.data.datasource

import com.example.meditrack.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getUser(userId: String): User? {
        return firestore.collection(USER_COLLECTION).document(userId).get().await().toObject()
    }

    suspend fun create(user: User) {
        firestore.collection(USER_COLLECTION).document(user.id).set(user).await()
    }

    suspend fun update(user: User) {
        firestore.collection(USER_COLLECTION).document(user.id).set(user).await()
    }

    suspend fun isKorisnickoImeZauzeto(korisnickoIme: String, korisnikId: String = ""): Boolean {
        val result = firestore.collection(USER_COLLECTION)
            .whereEqualTo("korisnickoIme", korisnickoIme)
            .limit(10)
            .get()
            .await()
        for (document in result.documents) {
            if (document.id != korisnikId) return true
        }
        return false
    }

    companion object {
        private const val USER_COLLECTION = "korisnici"
    }
}