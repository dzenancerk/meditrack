package com.example.meditrack.data.datasource

import com.example.meditrack.data.model.Podsjetnik
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class PodsjetniciDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getPodsjetnici(korisnikId: String): Flow<List<Podsjetnik>> {
        return firestore
            .collection(PODSJETNICI_COLLECTION)
            .whereEqualTo(KORISNIK_ID_FIELD, korisnikId)
            .snapshots()
            .map { it.toObjects() }
    }

    suspend fun getPodsjetnik(podsjetnikId: String): Podsjetnik? {
        return firestore
            .collection(PODSJETNICI_COLLECTION)
            .document(podsjetnikId)
            .get()
            .await()
            .toObject(Podsjetnik::class.java)
    }

    fun newId(): String = firestore.collection(PODSJETNICI_COLLECTION).document().id

    suspend fun create(podsjetnik: Podsjetnik) {
        firestore.collection(PODSJETNICI_COLLECTION).document(podsjetnik.id).set(podsjetnik).await()
    }

    suspend fun delete(podsjetnikId: String) {
        firestore.collection(PODSJETNICI_COLLECTION).document(podsjetnikId).delete().await()
    }

    suspend fun update(podsjetnik: Podsjetnik) {
        firestore.collection(PODSJETNICI_COLLECTION).document(podsjetnik.id).set(podsjetnik).await()
    }

    companion object {
        private const val PODSJETNICI_COLLECTION = "podsjetnici"
        private const val KORISNIK_ID_FIELD = "korisnikId"
    }
}