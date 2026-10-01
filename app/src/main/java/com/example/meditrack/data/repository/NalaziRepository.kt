package com.example.meditrack.data.repository

import com.example.meditrack.data.model.Nalaz
import com.example.meditrack.data.model.NalazSlika
import com.example.meditrack.data.model.SaveResult
import kotlinx.coroutines.flow.Flow

interface NalaziRepository {
    fun getNalazi(korisnikId: String): Flow<List<Nalaz>>
    fun generateId(): String
    suspend fun create(nalaz: Nalaz): SaveResult
    suspend fun getNalaz(nalazId: String): Nalaz?
    suspend fun delete(nalaz: Nalaz): SaveResult
    suspend fun update(nalaz: Nalaz, uklonjeneSlike: List<String> = emptyList()): SaveResult
    suspend fun resolveImageUrls(slike: List<NalazSlika>, userId: String): List<String>
}