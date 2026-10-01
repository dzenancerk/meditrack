package com.example.meditrack.data.datasource

import com.example.meditrack.data.model.Nalaz
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NalaziDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getNalazi(korisnikId: String): Flow<List<Nalaz>> {
        return firestore
            .collection(NALAZI_COLLECTION)
            .whereEqualTo(KORISNIK_ID_FIELD, korisnikId)
            .snapshots()
            .map { it.toObjects() }
    }

    fun newId(): String = firestore.collection(NALAZI_COLLECTION).document().id

    suspend fun create(nalaz: Nalaz) {
        firestore.collection(NALAZI_COLLECTION).document(nalaz.id).set(nalaz).await()
    }

    suspend fun delete(nalazId: String) {
        firestore.collection(NALAZI_COLLECTION).document(nalazId).delete().await()
    }

    suspend fun update(nalaz: Nalaz) {
        firestore.collection(NALAZI_COLLECTION).document(nalaz.id).set(nalaz).await()
    }

    suspend fun getNalaz(nalazId: String): Nalaz? {
        return firestore
            .collection(NALAZI_COLLECTION)
            .document(nalazId)
            .get()
            .await()
            .toObject(Nalaz::class.java)
    }

    companion object {
        private const val NALAZI_COLLECTION = "nalazi"
        private const val KORISNIK_ID_FIELD = "korisnikId"
    }
}