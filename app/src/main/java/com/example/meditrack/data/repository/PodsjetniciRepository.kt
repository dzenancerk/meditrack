package com.example.meditrack.data.repository

import com.example.meditrack.data.model.Podsjetnik
import com.example.meditrack.data.model.SaveResult
import kotlinx.coroutines.flow.Flow

interface PodsjetniciRepository {
    fun getPodsjetnici(korisnikId: String): Flow<List<Podsjetnik>>
    suspend fun getPodsjetnik(podsjetnikId: String): Podsjetnik?
    fun generateId(): String
    suspend fun create(podsjetnik: Podsjetnik): SaveResult
    suspend fun delete(podsjetnikId: String): SaveResult
    suspend fun update(podsjetnik: Podsjetnik): SaveResult
}