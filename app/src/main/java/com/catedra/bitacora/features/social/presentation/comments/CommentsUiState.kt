package com.catedra.bitacora.features.social.presentation.comments

// Estado de la pantalla de comentarios
import com.catedra.bitacora.features.social.domain.model.Comment

data class CommentsUiState(
    val comments: List<Comment> = emptyList(),
    val likedCommentIds: Set<String> = emptySet(),
    val likedReplyIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val currentCommentText: String = "",
    val currentUserId: String? = null,
    val replyingToId: String? = null,
    val replyingToName: String? = null,
    val parentCommentId: String? = null,
    val expandedCommentIds: Set<String> = emptySet(),
    val error: String? = null
)
