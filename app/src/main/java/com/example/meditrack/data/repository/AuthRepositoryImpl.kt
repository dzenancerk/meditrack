package com.example.meditrack.data.repository

import android.content.Context
import android.content.Intent
import com.example.meditrack.R
import com.example.meditrack.data.datasource.AuthRemoteDataSource
import com.example.meditrack.data.model.AuthUser
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthRemoteDataSource,
    @ApplicationContext private val context: Context
) : AuthRepository {
    override val currentUser: AuthUser?
        get() = authDataSource.currentUser?.let {
            AuthUser(
                uid = it.uid,
                email = it.email,
                isEmailVerified = it.isEmailVerified
            )
        }
    override suspend fun signIn(email: String, password: String) {
        authDataSource.signIn(email, password)
    }
    override suspend fun signUp(email: String, password: String) {
        authDataSource.signUp(email, password)
    }
    override fun signOut() {
        authDataSource.signOut()
    }
    override suspend fun signInWithGoogle(account: GoogleSignInAccount) {
        authDataSource.signInWithGoogle(account)
    }
    override suspend fun sendEmailVerification() {
        authDataSource.sendEmailVerification()
    }
    override suspend fun sendPasswordResetEmail(email: String) {
        authDataSource.sendPasswordResetEmail(email)
    }
    override fun isEmailVerified(): Boolean {
        return authDataSource.isEmailVerified()
    }
    override suspend fun reloadUser() {
        authDataSource.reloadUser()
    }
    override fun getGoogleSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.google_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut()
        return googleSignInClient.signInIntent
    }
}