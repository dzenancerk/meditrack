package com.example.meditrack.data.repository

import com.example.meditrack.data.model.KrvniPritisak
import com.example.meditrack.data.model.SaveResult
import kotlinx.coroutines.flow.Flow

interface KrvniPritisakRepository {
    fun getKrvniPritisak(korisnikId: String): Flow<List<KrvniPritisak>>
    fun generateId(): String
    suspend fun create(krvniPritisak: KrvniPritisak): SaveResult
    suspend fun delete(id: String): SaveResult
}