package com.catedra.bitacora.features.profile.data.repository

import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository
) : ProfileRepository {
    override suspend fun updateProfile(name: String, bio: String, photoUrl: String?): Result<Unit> {
        return authRepository.updateProfile(name, bio, photoUrl)
    }
}
