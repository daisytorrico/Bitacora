package com.catedra.bitacora.features.auth.domain.useCase

import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, pass: String): Result<Unit> {
        return repository.loginWithEmail(email, pass)
    }
}
