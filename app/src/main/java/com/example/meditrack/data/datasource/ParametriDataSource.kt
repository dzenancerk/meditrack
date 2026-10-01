package com.example.meditrack.data.datasource

import com.example.meditrack.data.model.Parametar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class ParametriDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getParametri(korisnikId: String): Flow<List<Parametar>> {
        return firestore
            .collection(PARAMETRI_COLLECTION)
            .whereEqualTo(KORISNIK_ID_FIELD, korisnikId)
            .snapshots()
            .map { it.toObjects() }
    }
    fun getParametriByTip(korisnikId: String, tip: String): Flow<List<Parametar>> {
        return firestore
            .collection(PARAMETRI_COLLECTION)
            .whereEqualTo(KORISNIK_ID_FIELD, korisnikId)
            .whereEqualTo("tip", tip)
            .snapshots()
            .map { it.toObjects() }
    }

    fun newId(): String = firestore.collection(PARAMETRI_COLLECTION).document().id

    suspend fun create(parametar: Parametar) {
        firestore.collection(PARAMETRI_COLLECTION).document(parametar.id).set(parametar).await()
    }

    suspend fun delete(parametarId: String) {
        firestore.collection(PARAMETRI_COLLECTION).document(parametarId).delete().await()
    }

    companion object {
        private const val PARAMETRI_COLLECTION = "parametri"
        private const val KORISNIK_ID_FIELD = "korisnikId"
    }
}