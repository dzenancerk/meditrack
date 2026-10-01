package com.example.meditrack.data.repository

import com.example.meditrack.data.model.User
import com.example.meditrack.data.model.SaveResult

interface UserRepository {
    suspend fun getUser(userId: String): User?
    suspend fun create(user: User)
    suspend fun update(user: User): SaveResult
    suspend fun isKorisnickoImeZauzeto(korisnickoIme: String, korisnikId: String = ""): Boolean
    suspend fun generisiJedinstvenoKorisnickoIme(baza: String): String
}