package com.example.meditrack.data.repository

import com.example.meditrack.data.datasource.KrvniPritisakDataSource
import com.example.meditrack.data.model.KrvniPritisak
import com.example.meditrack.data.model.SaveResult
import com.example.meditrack.data.repository.NetworkConstants.WRITE_TIMEOUT_MS
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class KrvniPritisakRepositoryImpl @Inject constructor(
    private val dataSource: KrvniPritisakDataSource
) : KrvniPritisakRepository {
    override fun getKrvniPritisak(korisnikId: String): Flow<List<KrvniPritisak>> {
        return dataSource.getKrvniPritisak(korisnikId)
    }
    override fun generateId(): String = dataSource.newId()
    override suspend fun create(krvniPritisak: KrvniPritisak): SaveResult {
        val confirmed = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            dataSource.create(krvniPritisak)
            true
        }
        return if (confirmed == true) SaveResult.CONFIRMED else SaveResult.TIMED_OUT
    }
    override suspend fun delete(id: String): SaveResult {
        val confirmed = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            dataSource.delete(id)
            true
        }
        return if (confirmed == true) SaveResult.CONFIRMED else SaveResult.TIMED_OUT
    }
}