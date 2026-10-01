package com.example.meditrack.data.repository

import com.example.meditrack.data.datasource.UserDataSource
import com.example.meditrack.data.model.User
import javax.inject.Inject
import com.example.meditrack.data.model.SaveResult
import com.example.meditrack.data.repository.NetworkConstants.WRITE_TIMEOUT_MS
import kotlinx.coroutines.withTimeoutOrNull

class UserRepositoryImpl @Inject constructor(
    private val userDataSource: UserDataSource
) : UserRepository {
    override suspend fun getUser(userId: String): User? {
        return userDataSource.getUser(userId)
    }
    override suspend fun create(user: User) {
        userDataSource.create(user)
    }
    override suspend fun update(user: User): SaveResult {
        val confirmed = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            userDataSource.update(user)
            true
        }
        return if (confirmed == true) SaveResult.CONFIRMED else SaveResult.TIMED_OUT
    }
    override suspend fun isKorisnickoImeZauzeto(korisnickoIme: String, korisnikId: String): Boolean {
        return userDataSource.isKorisnickoImeZauzeto(korisnickoIme, korisnikId)
    }
    override suspend fun generisiJedinstvenoKorisnickoIme(baza: String): String {
        val ociscenaBaza = baza.ifBlank { "korisnik" }
        if (!isKorisnickoImeZauzeto(ociscenaBaza)) return ociscenaBaza
        for (brojac in 2..1000) {
            val kandidat = "$ociscenaBaza$brojac"
            if (!isKorisnickoImeZauzeto(kandidat)) return kandidat
        }
        return "$ociscenaBaza${System.currentTimeMillis()}"
    }
}