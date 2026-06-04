package com.catedra.bitacora.features.discovery.presentation.publicPointDetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.discovery.domain.useCase.GetPublicPointDetailUseCase
import com.catedra.bitacora.features.discovery.domain.useCase.GetPublicProfileUseCase
import com.catedra.bitacora.features.social.domain.useCase.*
import com.catedra.bitacora.features.travel.presentation.pointDetail.PointDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PublicPointDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val useCase: GetPublicPointDetailUseCase,
    private val profileUseCase: GetPublicProfileUseCase,
    private val sessionRepository: com.catedra.bitacora.core.domain.repository.SessionRepository,
    private val likeUseCases: LikeUseCases,
    private val commentUseCases: CommentUseCases
) : ViewModel() {
    private val travelId: String = checkNotNull(savedStateHandle["travelId"])
    private val pointId: String = checkNotNull(savedStateHandle["pointId"])
    private val ownerId: String = checkNotNull(savedStateHandle["ownerId"])

    private val _uiState = MutableStateFlow(PointDetailUiState(
        travelId = travelId,
        pointId = pointId,
        currentUserId = sessionRepository.getCurrentUser()?.uid
    ))
    val uiState: StateFlow<PointDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
        observeSocialData()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val pointResult = useCase(travelId, pointId)
            val userResult = profileUseCase.getUser(ownerId)
            val currentUserId = sessionRepository.getCurrentUser()?.uid

            _uiState.update { it.copy(
                point = pointResult.getOrNull(),
                creatorUser = userResult.getOrNull(),
                isOwner = ownerId == currentUserId,
                isLoading = false,
                error = pointResult.exceptionOrNull()?.message
            ) }
        }
    }

    private fun observeSocialData() {
        likeUseCases.getLikesCount(travelId, pointId)
            .onEach { count -> _uiState.update { it.copy(likesCount = count) } }
            .launchIn(viewModelScope)

        // Carga el estado inicial una sola vez para evitar race condition con el optimistic update
        viewModelScope.launch {
            likeUseCases.isLiked(LikeTarget.POI, travelId, pointId)
                .first()
                .let { isLiked -> _uiState.update { it.copy(isLiked = isLiked) } }
        }

        commentUseCases.getComments(travelId, pointId)
            .onEach { list -> _uiState.update { it.copy(commentsCount = list.size) } }
            .launchIn(viewModelScope)
    }

    fun toggleLike() {
        val currentIsLiked = uiState.value.isLiked
        val currentLikesCount = uiState.value.likesCount

        _uiState.update { it.copy(
            isLiked = !currentIsLiked,
            likesCount = if (currentIsLiked) currentLikesCount - 1 else currentLikesCount + 1
        ) }

        viewModelScope.launch {
            val result = likeUseCases.toggleLike(
                target = LikeTarget.POI,
                tripId = travelId,
                poiId = pointId,
                isLiked = currentIsLiked
            )

            if (result.isFailure) {
                Log.e("POI_LIKE", "Error: ${result.exceptionOrNull()?.message}", result.exceptionOrNull())
                _uiState.update { it.copy(
                    isLiked = currentIsLiked,
                    likesCount = currentLikesCount
                ) }
            }
        }
    }

    fun onToggleMap(show: Boolean) {
        _uiState.update { it.copy(showMap = show) }
    }
}