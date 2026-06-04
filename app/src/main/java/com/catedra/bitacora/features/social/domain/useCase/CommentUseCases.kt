package com.catedra.bitacora.features.social.domain.useCase

// Casos de uso para obtener, guardar y borrar comentarios
import com.catedra.bitacora.features.social.domain.model.Comment
import com.catedra.bitacora.features.social.domain.repository.SocialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CommentUseCases @Inject constructor(
    private val repository: SocialRepository
) {
    fun getComments(tripId: String, poiId: String): Flow<List<Comment>> = repository.getComments(tripId, poiId)
    
    suspend fun saveComment(tripId: String, poiId: String, content: String, parentId: String? = null) = 
        repository.saveComment(tripId, poiId, content, parentId)
    
    suspend fun deleteMessage(tripId: String, poiId: String, commentId: String, replyId: String? = null) = 
        repository.deleteSocialMessage(tripId, poiId, commentId, replyId)
}
