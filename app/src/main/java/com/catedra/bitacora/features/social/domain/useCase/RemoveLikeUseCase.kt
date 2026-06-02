package com.catedra.bitacora.features.social.domain.useCase

import com.catedra.bitacora.features.social.domain.repository.SocialRepository
import javax.inject.Inject

class RemoveLikeUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(tripId: String, poiId: String) = repository.removeLike(tripId, poiId)
}