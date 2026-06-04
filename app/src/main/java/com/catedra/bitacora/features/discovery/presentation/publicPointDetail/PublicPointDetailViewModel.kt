package com.catedra.bitacora.features.discovery.presentation.publicPointDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.discovery.domain.useCase.GetPublicPointDetailUseCase
import com.catedra.bitacora.features.discovery.domain.useCase.GetPublicProfileUseCase
import com.catedra.bitacora.features.discovery.domain.useCase.GetPublicTravelDetailUseCase
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
    private val travelUseCase: GetPublicTravelDetailUseCase,
    private val profileUseCase: GetPublicProfileUseCase,
    private val authRepository: AuthRepository,
    private val giveLikeUseCase: GiveLikeUseCase,
    private val removeLikeUseCase: RemoveLikeUseCase,
    private val getLikesCountUseCase: GetLikesCountUseCase,
    private val getIsLikedUseCase: GetIsLikedUseCase,
    private val getCommentsCountUseCase: GetCommentsCountUseCase
) : ViewModel() {
    val travelId: String = checkNotNull(savedStateHandle["travelId"])
    val pointId: String = checkNotNull(savedStateHandle["pointId"])
    val ownerId: String = checkNotNull(savedStateHandle["ownerId"])

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
            val travelResult = travelUseCase.getTravel(travelId)
            val userResult = profileUseCase.getUser(ownerId)
            val currentUserId = authRepository.getCurrentUser()?.uid

            val isOwner = travelResult.getOrNull()?.ownerId == currentUserId
            val canEdit = isOwner || travelResult.getOrNull()?.privileges?.contains(currentUserId) == true

            _uiState.update { it.copy(
                point = pointResult.getOrNull(),
                creatorUser = userResult.getOrNull(),
                isOwner = isOwner,
                canEdit = canEdit,
                isLoading = false,
                error = pointResult.exceptionOrNull()?.message
            ) }
        }
    }

    private fun observeSocialData() {
        getLikesCountUseCase(travelId, pointId)
            .onEach { count -> _uiState.update { it.copy(likesCount = count) } }
            .launchIn(viewModelScope)

        getIsLikedUseCase(travelId, pointId)
            .onEach { isLiked -> _uiState.update { it.copy(isLiked = isLiked) } }
            .launchIn(viewModelScope)

        getCommentsCountUseCase(travelId, pointId)
            .onEach { count -> _uiState.update { it.copy(commentsCount = count) } }
            .launchIn(viewModelScope)
    }

    fun toggleLike() {
        viewModelScope.launch {
            val isLiked = uiState.value.isLiked
            if (isLiked) {
                removeLikeUseCase(travelId, pointId)
            } else {
                giveLikeUseCase(travelId, pointId)
            }
        }
    }

    fun onToggleMap(show: Boolean) {
        _uiState.update { it.copy(showMap = show) }
    }
}
