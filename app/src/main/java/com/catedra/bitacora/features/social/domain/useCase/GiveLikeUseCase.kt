package com.catedra.bitacora.features.social.domain.useCase

import com.catedra.bitacora.features.social.domain.repository.SocialRepository
import javax.inject.Inject

class GiveLikeUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(tripId: String, poiId: String) = repository.giveLike(tripId, poiId)
}