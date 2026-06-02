package com.catedra.bitacora.features.travel.presentation.pointDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.social.domain.useCase.*
import com.catedra.bitacora.features.travel.domain.useCase.GetPointOfInterestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PointDetailViewModel @Inject constructor(
    private val getPointOfInterestUseCase: GetPointOfInterestUseCase,
    private val authRepository: AuthRepository,
    private val giveLikeUseCase: GiveLikeUseCase,
    private val removeLikeUseCase: RemoveLikeUseCase,
    private val getLikesCountUseCase: GetLikesCountUseCase,
    private val getIsLikedUseCase: GetIsLikedUseCase,
    private val getCommentsCountUseCase: GetCommentsCountUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val travelId: String = checkNotNull(savedStateHandle["travelId"])
    private val pointId: String = checkNotNull(savedStateHandle["pointId"])

    private val _uiState = MutableStateFlow(PointDetailUiState())
    val uiState: StateFlow<PointDetailUiState> = _uiState.asStateFlow()

    init {
        loadPointDetail()
        observeSocialData()
    }

    private fun loadPointDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val pointResult = getPointOfInterestUseCase(travelId, pointId)
            val userResult = authRepository.getFullUserData()

            if (pointResult.isSuccess) {
                val user = userResult.getOrNull()
                val currentUserId = authRepository.getCurrentUser()?.uid
                _uiState.update { it.copy(
                    isLoading = false, 
                    point = pointResult.getOrNull(),
                    creatorUser = user,
                    isOwner = user?.uid == currentUserId,
                    error = null
                ) }
            } else {
                _uiState.update { it.copy(
                    isLoading = false, 
                    error = pointResult.exceptionOrNull()?.message 
                ) }
            }
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
