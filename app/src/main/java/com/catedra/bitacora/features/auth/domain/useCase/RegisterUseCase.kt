package com.catedra.bitacora.features.auth.domain.useCase

import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(nombre: String, email: String, pass: String): Result<Unit> {
        return repository.registerWithEmail(nombre, email, pass)
    }
}
