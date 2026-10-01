package com.example.meditrack.data.repository

import com.example.meditrack.data.datasource.NalaziDataSource
import com.example.meditrack.data.model.Nalaz
import com.example.meditrack.data.model.NalazSlika
import com.example.meditrack.data.model.SaveResult
import com.example.meditrack.data.repository.NetworkConstants.WRITE_TIMEOUT_MS
import com.example.meditrack.data.storage.NalazImageStorage
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class NalaziRepositoryImpl @Inject constructor(
    private val nalaziDataSource: NalaziDataSource,
    private val nalazImageStorage: NalazImageStorage
) : NalaziRepository {
    override fun getNalazi(korisnikId: String): Flow<List<Nalaz>> {
        return nalaziDataSource.getNalazi(korisnikId)
    }
    override fun generateId(): String = nalaziDataSource.newId()
    override suspend fun create(nalaz: Nalaz): SaveResult {
        val confirmed = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            nalaziDataSource.create(nalaz)
            true
        }
        return if (confirmed == true) SaveResult.CONFIRMED else SaveResult.TIMED_OUT
    }
    override suspend fun getNalaz(nalazId: String): Nalaz? {
        return nalaziDataSource.getNalaz(nalazId)
    }
    override suspend fun delete(nalaz: Nalaz): SaveResult {
        val confirmed = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            nalaziDataSource.delete(nalaz.id)
            true
        }
        if (confirmed == true) {
            nalazImageStorage.deleteImages(nalaz.fileUrls)
        }
        return if (confirmed == true) SaveResult.CONFIRMED else SaveResult.TIMED_OUT
    }
    override suspend fun update(nalaz: Nalaz, uklonjeneSlike: List<String>): SaveResult {
        val confirmed = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            nalaziDataSource.update(nalaz)
            true
        }
        if (confirmed == true && uklonjeneSlike.isNotEmpty()) {
            nalazImageStorage.deleteImages(uklonjeneSlike)
        }
        return if (confirmed == true) SaveResult.CONFIRMED else SaveResult.TIMED_OUT
    }
    override suspend fun resolveImageUrls(slike: List<NalazSlika>, userId: String): List<String> {
        return slike.map { slika ->
            when (slika) {
                is NalazSlika.Postojeca -> slika.url
                is NalazSlika.Nova -> nalazImageStorage.copyImageLocally(slika.uri, userId)
                    ?: throw Exception("Nije moguće sačuvati jednu od slika")
            }
        }
    }
}