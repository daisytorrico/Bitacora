package com.catedra.bitacora.features.social.domain.useCase

import com.catedra.bitacora.features.social.domain.repository.SocialRepository
import javax.inject.Inject

class UnfollowUserUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(targetUserId: String) = repository.unfollowUser(targetUserId)
}