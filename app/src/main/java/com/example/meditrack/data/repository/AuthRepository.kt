package com.example.meditrack.data.repository

import android.content.Intent
import com.example.meditrack.data.model.AuthUser
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface AuthRepository {
    val currentUser: AuthUser?
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    fun signOut()
    suspend fun signInWithGoogle(account: GoogleSignInAccount)
    suspend fun sendEmailVerification()
    suspend fun sendPasswordResetEmail(email: String)
    fun isEmailVerified(): Boolean
    suspend fun reloadUser()
    fun getGoogleSignInIntent(): Intent
}