package com.catedra.bitacora.features.travel.presentation.pointDetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.social.domain.useCase.*
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import com.catedra.bitacora.features.travel.domain.useCase.GetPointOfInterestUseCase
import com.catedra.bitacora.features.travel.domain.useCase.DeletePointUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PointDetailViewModel @Inject constructor(
    private val getPointOfInterestUseCase: GetPointOfInterestUseCase,
    private val deletePointUseCase: DeletePointUseCase,
    private val travelsRepository: TravelsRepository,
    private val authRepository: AuthRepository,
    private val likeUseCases: LikeUseCases,
    private val commentUseCases: CommentUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val travelId: String = checkNotNull(savedStateHandle["travelId"])
    private val pointId: String = checkNotNull(savedStateHandle["pointId"])

    private val _uiState = MutableStateFlow(PointDetailUiState(
        travelId = travelId,
        pointId = pointId,
        currentUserId = authRepository.getCurrentUser()?.uid
    ))
    val uiState: StateFlow<PointDetailUiState> = _uiState.asStateFlow()

    init {
        loadPointDetail()
        observeSocialData()
    }

    private fun loadPointDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val pointResult = getPointOfInterestUseCase(travelId, pointId)
            val travelResult = travelsRepository.getTravelById(travelId)
            val userResult = authRepository.getFullUserData()

            if (pointResult.isSuccess && travelResult.isSuccess) {
                val travel = travelResult.getOrNull()!!
                val user = userResult.getOrNull()
                val currentUserId = authRepository.getCurrentUser()?.uid
                val isOwner = travel.ownerId == currentUserId
                val canEdit = isOwner || travel.privileges?.contains(currentUserId) == true
                
                _uiState.update { it.copy(
                    isLoading = false,
                    point = pointResult.getOrNull(),
                    creatorUser = user,
                    isOwner = isOwner,
                    canEdit = canEdit,
                    error = null
                ) }
            } else {
                _uiState.update { it.copy(
                    isLoading = false, 
                    error = pointResult.exceptionOrNull()?.message ?: travelResult.exceptionOrNull()?.message
                ) }
            }
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

    fun setShowDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    fun deletePoint() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteDialog = false) }
            val result = deletePointUseCase(travelId, pointId)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isDeleted = true) }
            } else {
                _uiState.update { it.copy(
                    isLoading = false, 
                    error = result.exceptionOrNull()?.message ?: "Error al eliminar el punto"
                ) }
            }
        }
    }
}
