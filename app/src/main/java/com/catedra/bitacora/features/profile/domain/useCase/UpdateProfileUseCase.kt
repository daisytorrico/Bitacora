package com.catedra.bitacora.features.profile.domain.useCase

import com.catedra.bitacora.features.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(name: String, bio: String, photoUrl: String?): Result<Unit> {
        return repository.updateProfile(name, bio, photoUrl)
    }
}
