package com.catedra.bitacora.features.social.domain.useCase

// Casos de uso para obtener respuestas de un comentario
import com.catedra.bitacora.features.social.domain.model.Comment
import com.catedra.bitacora.features.social.domain.repository.SocialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReplyUseCases @Inject constructor(
    private val repository: SocialRepository
) {
    fun getReplies(tripId: String, poiId: String, commentId: String): Flow<List<Comment>> = repository.getReplies(tripId, poiId, commentId)
}
