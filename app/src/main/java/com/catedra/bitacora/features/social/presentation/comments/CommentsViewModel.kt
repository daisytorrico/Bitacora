package com.catedra.bitacora.features.social.presentation.comments

// Logica de comentarios y respuestas
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.core.domain.repository.SessionRepository
import com.catedra.bitacora.features.social.domain.model.Comment
import com.catedra.bitacora.features.social.domain.useCase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val getCommentsWithSocialDataUseCase: GetCommentsWithSocialDataUseCase,
    private val commentUseCases: CommentUseCases,
    private val likeUseCases: LikeUseCases
) : ViewModel() {

    private val travelId: String = checkNotNull(savedStateHandle["travelId"])
    private val pointId: String = checkNotNull(savedStateHandle["pointId"])

    private val _uiState = MutableStateFlow(CommentsUiState(currentUserId = sessionRepository.getCurrentUser()?.uid))
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    init {
        observeComments()
    }

    private fun observeComments() {
        getCommentsWithSocialDataUseCase(travelId, pointId)
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { data ->
                _uiState.update {
                    it.copy(
                        comments = data.comments,
                        likedCommentIds = data.likedCommentIds,
                        likedReplyIds = data.likedReplyIds,
                        isLoading = false
                    )
                }
            }
            .catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun toggleReplies(commentId: String) {
        _uiState.update { state ->
            val newExpanded = if (state.expandedCommentIds.contains(commentId)) {
                state.expandedCommentIds - commentId
            } else {
                state.expandedCommentIds + commentId
            }
            state.copy(expandedCommentIds = newExpanded)
        }
    }

    fun onCommentTextChange(newText: String) {
        _uiState.update { it.copy(currentCommentText = newText) }
    }

    fun setReplyingTo(commentId: String, name: String, parentId: String?) {
        _uiState.update {
            it.copy(
                replyingToId = commentId,
                replyingToName = name,
                parentCommentId = parentId ?: commentId
            )
        }
    }

    fun toggleLike(commentId: String) {
        val isLiked = uiState.value.likedCommentIds.contains(commentId)
        viewModelScope.launch {
            likeUseCases.toggleLike(
                target = LikeTarget.COMMENT,
                tripId = travelId,
                poiId = pointId,
                commentId = commentId,
                isLiked = isLiked
            )
        }
    }

    fun toggleReplyLike(commentId: String, replyId: String) {
        val isLiked = uiState.value.likedReplyIds.contains(replyId)
        viewModelScope.launch {
            likeUseCases.toggleLike(
                target = LikeTarget.REPLY,
                tripId = travelId,
                poiId = pointId,
                commentId = commentId,
                replyId = replyId,
                isLiked = isLiked
            )
            // likedReplyIds se actualiza desde el flow reactivo, no hace falta update manual
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            commentUseCases.deleteMessage(travelId, pointId, commentId)
        }
    }

    fun deleteReply(commentId: String, replyId: String) {
        viewModelScope.launch {
            commentUseCases.deleteMessage(travelId, pointId, commentId, replyId)
        }
    }

    fun sendComment() {
        val text = uiState.value.currentCommentText.trim()
        if (text.isEmpty()) return

        val parentId = uiState.value.parentCommentId

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }

            commentUseCases.saveComment(travelId, pointId, text, parentId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            currentCommentText = "",
                            parentCommentId = null,
                            replyingToId = null,
                            replyingToName = null
                        )
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isSending = false, error = e.message) }
                }
        }
    }
}