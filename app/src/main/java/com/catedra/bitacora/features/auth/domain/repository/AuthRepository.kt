package com.catedra.bitacora.features.auth.domain.repository

import com.catedra.bitacora.features.auth.domain.model.AuthState
import com.catedra.bitacora.features.auth.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    
    suspend fun loginWithEmail(email: String, pass: String): Result<Unit>
    suspend fun registerWithEmail(nombre: String, email: String, pass: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun logout()
    suspend fun saveUsername(username: String): Result<Unit>
    fun getCurrentUser(): User?
    fun isUserLoggedIn(): Boolean
}
