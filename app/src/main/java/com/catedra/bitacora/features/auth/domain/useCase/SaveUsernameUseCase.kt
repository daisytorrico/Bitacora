package com.catedra.bitacora.features.auth.domain.useCase

import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class SaveUsernameUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(username: String): Result<Unit> {
        return repository.saveUsername(username)
    }
}
