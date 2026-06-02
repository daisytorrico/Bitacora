package com.catedra.bitacora.features.auth.domain.useCase

import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GetFullUserDataUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User> {
        return authRepository.getFullUserData()
    }
}