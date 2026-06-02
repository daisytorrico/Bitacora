package com.catedra.bitacora.features.social.domain.useCase

import com.catedra.bitacora.features.social.domain.repository.SocialRepository
import javax.inject.Inject

class SaveReplyUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(tripId: String, poiId: String, commentId: String, content: String) = 
        repository.saveReply(tripId, poiId, commentId, content)
}