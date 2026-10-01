package com.example.meditrack.data.repository

import com.example.meditrack.data.model.Parametar
import com.example.meditrack.data.model.SaveResult
import kotlinx.coroutines.flow.Flow

interface ParametriRepository {
    fun getParametri(korisnikId: String): Flow<List<Parametar>>
    fun getParametriByTip(korisnikId: String, tip: String): Flow<List<Parametar>>
    fun generateId(): String
    suspend fun create(parametar: Parametar): SaveResult
    suspend fun delete(parametarId: String): SaveResult
}