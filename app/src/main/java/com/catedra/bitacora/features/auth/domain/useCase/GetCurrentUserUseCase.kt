package com.catedra.bitacora.features.auth.domain.useCase

import com.catedra.bitacora.features.auth.domain.model.User
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}