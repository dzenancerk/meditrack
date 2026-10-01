package com.example.meditrack.data.repository

import com.example.meditrack.data.datasource.ParametriDataSource
import com.example.meditrack.data.model.Parametar
import com.example.meditrack.data.model.SaveResult
import com.example.meditrack.data.repository.NetworkConstants.WRITE_TIMEOUT_MS
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ParametriRepositoryImpl @Inject constructor(
    private val parametriDataSource: ParametriDataSource
) : ParametriRepository {
    override fun getParametri(korisnikId: String): Flow<List<Parametar>> {
        return parametriDataSource.getParametri(korisnikId)
    }
    override fun getParametriByTip(korisnikId: String, tip: String): Flow<List<Parametar>> {
        return parametriDataSource.getParametriByTip(korisnikId, tip)
    }
    override fun generateId(): String = parametriDataSource.newId()
    override suspend fun create(parametar: Parametar): SaveResult {
        val confirmed = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            parametriDataSource.create(parametar)
            true
        }
        return if (confirmed == true) SaveResult.CONFIRMED else SaveResult.TIMED_OUT
    }
    override suspend fun delete(parametarId: String): SaveResult {
        val confirmed = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            parametriDataSource.delete(parametarId)
            true
        }
        return if (confirmed == true) SaveResult.CONFIRMED else SaveResult.TIMED_OUT
    }
}