package com.catedra.bitacora.features.auth.domain.useCase

import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GetUsersByIdsUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(uids: List<String>): Result<List<User>> {
        if (uids.isEmpty()) return Result.success(emptyList())
        return authRepository.getUsersByIds(uids)
    }
}
