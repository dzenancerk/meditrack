package com.example.meditrack.data.repository

import com.example.meditrack.data.datasource.PodsjetniciDataSource
import com.example.meditrack.data.model.Podsjetnik
import com.example.meditrack.data.model.SaveResult
import com.example.meditrack.data.repository.NetworkConstants.WRITE_TIMEOUT_MS
import javax.inject.Inject
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.Flow

class PodsjetniciRepositoryImpl @Inject constructor(
    private val podsjetniciDataSource: PodsjetniciDataSource
) : PodsjetniciRepository {
    override fun getPodsjetnici(korisnikId: String): Flow<List<Podsjetnik>> {
        return podsjetniciDataSource.getPodsjetnici(korisnikId)
    }
    override suspend fun getPodsjetnik(podsjetnikId: String): Podsjetnik? {
        return podsjetniciDataSource.getPodsjetnik(podsjetnikId)
    }
    override fun generateId(): String = podsjetniciDataSource.newId()
    override suspend fun create(podsjetnik: Podsjetnik): SaveResult {
        val confirmed = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            podsjetniciDataSource.create(podsjetnik)
            true
        }
        return if (confirmed == true) SaveResult.CONFIRMED else SaveResult.TIMED_OUT
    }
    override suspend fun delete(podsjetnikId: String): SaveResult {
        val confirmed = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            podsjetniciDataSource.delete(podsjetnikId)
            true
        }
        return if (confirmed == true) SaveResult.CONFIRMED else SaveResult.TIMED_OUT
    }
    override suspend fun update(podsjetnik: Podsjetnik): SaveResult {
        val confirmed = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            podsjetniciDataSource.update(podsjetnik)
            true
        }
        return if (confirmed == true) SaveResult.CONFIRMED else SaveResult.TIMED_OUT
    }
}