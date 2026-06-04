package com.catedra.bitacora.features.auth.domain.useCase

import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class SearchUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(query: String): Result<List<User>> {
        if (query.isBlank()) return Result.success(emptyList())
        return authRepository.searchUsers(query)
    }
}
