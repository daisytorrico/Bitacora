package com.catedra.bitacora.features.social.domain.useCase

// Casos de uso para seguir y dejar de seguir usuarios
import com.catedra.bitacora.features.social.domain.repository.SocialRepository
import javax.inject.Inject

class FollowUseCases @Inject constructor(
    private val repository: SocialRepository
) {
    suspend fun toggleFollow(userId: String, currentIsFollowing: Boolean): Result<Unit> {
        return if (currentIsFollowing) {
            repository.unfollowUser(userId)
        } else {
            repository.followUser(userId)
        }
    }

    suspend fun isFollowing(userId: String) = repository.isFollowing(userId)
}
