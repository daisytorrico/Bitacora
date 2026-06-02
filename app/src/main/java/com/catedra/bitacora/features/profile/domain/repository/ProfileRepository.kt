package com.catedra.bitacora.features.profile.domain.repository

interface ProfileRepository {
    suspend fun updateProfile(name: String, bio: String, photoUrl: String?): Result<Unit>
}
