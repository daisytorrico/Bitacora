package com.catedra.bitacora.features.auth.domain.repository

import com.catedra.bitacora.core.domain.model.AuthState
import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository : SessionRepository {
    override val authState: StateFlow<AuthState>
    
    suspend fun loginWithEmail(email: String, pass: String): Result<Unit>
    suspend fun registerWithEmail(nombre: String, email: String, pass: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun logout()
    suspend fun saveUsername(username: String): Result<Unit>
    override suspend fun getFullUserData(): Result<User>
    suspend fun updateProfile(name: String, bio: String, photoUrl: String?): Result<Unit>
    override fun getCurrentUser(): User?
    override fun isUserLoggedIn(): Boolean
}
