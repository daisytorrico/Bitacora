package com.catedra.bitacora.core.domain.repository

import com.catedra.bitacora.core.domain.model.AuthState
import com.catedra.bitacora.core.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {
    val authState: StateFlow<AuthState>
    fun getCurrentUser(): User?
    suspend fun getFullUserData(): Result<User>
    fun isUserLoggedIn(): Boolean
}
