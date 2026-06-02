package com.catedra.bitacora.features.social.domain.useCase

import com.catedra.bitacora.features.social.domain.repository.SocialRepository
import javax.inject.Inject

class GetIsLikedUseCase @Inject constructor(private val repository: SocialRepository) {
    operator fun invoke(tripId: String, poiId: String) = repository.getIsLiked(tripId, poiId)
}