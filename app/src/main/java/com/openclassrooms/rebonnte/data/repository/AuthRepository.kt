package com.openclassrooms.rebonnte.data.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    fun currentUser(): FirebaseUser?
    suspend fun signIn(email: String, password: String): Result<FirebaseUser>
    suspend fun register(email: String, password: String): Result<FirebaseUser>
    fun signOut()
}
