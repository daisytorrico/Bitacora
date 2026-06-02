package com.catedra.bitacora.features.discovery.domain.useCase

import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.features.discovery.domain.repository.DiscoveryRepository
import com.catedra.bitacora.features.travel.domain.model.Travel
import javax.inject.Inject

class GetPublicProfileUseCase @Inject constructor(
    private val repository: DiscoveryRepository
) {
    suspend fun getUser(userId: String): Result<User> = repository.getPublicProfile(userId)
    suspend fun getTravels(userId: String): Result<List<Travel>> = repository.getPublicUserTravels(userId)
    suspend fun isFollowing(userId: String): Result<Boolean> = repository.isFollowing(userId)
    suspend fun toggleFollow(userId: String, currentStatus: Boolean): Result<Unit> {
        return if (currentStatus) repository.unfollowUser(userId) else repository.followUser(userId)
    }
}
