package com.catedra.bitacora.features.auth.domain.useCase

import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(name: String, bio: String, photoUrl: String?): Result<Unit> {
        return repository.updateProfile(name, bio, photoUrl)
    }
}
