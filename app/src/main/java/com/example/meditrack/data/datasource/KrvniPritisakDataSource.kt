package com.example.meditrack.data.datasource

import com.example.meditrack.data.model.KrvniPritisak
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class KrvniPritisakDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getKrvniPritisak(korisnikId: String): Flow<List<KrvniPritisak>> {
        return firestore
            .collection(COLLECTION)
            .whereEqualTo(KORISNIK_ID_FIELD, korisnikId)
            .snapshots()
            .map { it.toObjects() }
    }

    fun newId(): String = firestore.collection(COLLECTION).document().id

    suspend fun create(krvniPritisak: KrvniPritisak) {
        firestore.collection(COLLECTION).document(krvniPritisak.id).set(krvniPritisak).await()
    }

    suspend fun delete(id: String) {
        firestore.collection(COLLECTION).document(id).delete().await()
    }

    companion object {
        private const val COLLECTION = "krvni_pritisak"
        private const val KORISNIK_ID_FIELD = "korisnikId"
    }
}