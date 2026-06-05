package com.catedra.bitacora.features.discovery.presentation.publicPointDetail

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
    private val likeUseCases: LikeUseCases,
    private val commentUseCases: CommentUseCases
) : ViewModel() {
    private val travelId: String = checkNotNull(savedStateHandle["travelId"])
    private val pointId: String = checkNotNull(savedStateHandle["pointId"])
    private val ownerId: String = checkNotNull(savedStateHandle["ownerId"])

    private val _uiState = MutableStateFlow(PointDetailUiState())
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

            _uiState.update { it.copy(
                point = pointResult.getOrNull(),
                creatorUser = userResult.getOrNull(),
                isOwner = false,
                isLoading = false,
                error = pointResult.exceptionOrNull()?.message
            ) }
        }
    }

    private fun observeSocialData() {
        likeUseCases.getLikesCount(travelId, pointId)
            .onEach { count -> _uiState.update { it.copy(likesCount = count) } }
            .launchIn(viewModelScope)

        likeUseCases.isLiked(LikeTarget.POI, travelId, pointId)
            .onEach { isLiked -> _uiState.update { it.copy(isLiked = isLiked) } }
            .launchIn(viewModelScope)

        commentUseCases.getComments(travelId, pointId)
            .onEach { list -> _uiState.update { it.copy(commentsCount = list.size) } }
            .launchIn(viewModelScope)
    }

    fun toggleLike() {
        val currentIsLiked = uiState.value.isLiked
        viewModelScope.launch {
            likeUseCases.toggleLike(
                target = LikeTarget.POI,
                tripId = travelId,
                poiId = pointId,
                isLiked = currentIsLiked
            )
        }
    }

    fun onToggleMap(show: Boolean) {
        _uiState.update { it.copy(showMap = show) }
    }
}
