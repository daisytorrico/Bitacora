package com.catedra.bitacora.features.social.domain.useCase

// Casos de uso para gestionar 'Me gusta' en POIs, comentarios y respuestas
import com.catedra.bitacora.features.social.domain.repository.SocialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

enum class LikeTarget {
    POI, COMMENT, REPLY
}

class LikeUseCases @Inject constructor(
    private val repository: SocialRepository
) {
    suspend fun toggleLike(
        target: LikeTarget,
        tripId: String,
        poiId: String,
        commentId: String? = null,
        replyId: String? = null,
        isLiked: Boolean
    ): Result<Unit> {
        val path = when (target) {
            LikeTarget.POI -> "trips/$tripId/pointsOfInterest/$poiId/likes"
            LikeTarget.COMMENT -> "trips/$tripId/pointsOfInterest/$poiId/comments/$commentId/likes"
            LikeTarget.REPLY -> "trips/$tripId/pointsOfInterest/$poiId/comments/$commentId/replies/$replyId/likes"
        }
        return repository.toggleLike(path, isLiked)
    }

    fun isLiked(
        target: LikeTarget,
        tripId: String,
        poiId: String,
        commentId: String? = null,
        replyId: String? = null
    ): Flow<Boolean> {
        val path = when (target) {
            LikeTarget.POI -> "trips/$tripId/pointsOfInterest/$poiId/likes"
            LikeTarget.COMMENT -> "trips/$tripId/pointsOfInterest/$poiId/comments/$commentId/likes"
            LikeTarget.REPLY -> "trips/$tripId/pointsOfInterest/$poiId/comments/$commentId/replies/$replyId/likes"
        }
        return repository.getIsLiked(path)
    }

    fun getLikesCount(tripId: String, poiId: String): Flow<Int> = repository.getLikesCount(tripId, poiId)
}
