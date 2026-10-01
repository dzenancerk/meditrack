package com.example.meditrack.data.model

data class AuthUser(
    val uid: String,
    val email: String?,
    val isEmailVerified: Boolean
)