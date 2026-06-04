package com.catedra.bitacora.features.discovery.presentation.publicTravelDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.discovery.domain.useCase.GetPublicTravelDetailUseCase
import com.catedra.bitacora.features.discovery.domain.useCase.GetPublicProfileUseCase
import com.catedra.bitacora.features.travel.presentation.travelDetail.TravelDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PublicTravelDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val useCase: GetPublicTravelDetailUseCase,
    private val profileUseCase: GetPublicProfileUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val travelId: String = checkNotNull(savedStateHandle["travelId"])

    private val _uiState = MutableStateFlow(TravelDetailUiState())
    val uiState: StateFlow<TravelDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val travelResult = useCase.getTravel(travelId)
            
            travelResult.onSuccess { travel ->
                val pointsResult = useCase.getPoints(travelId)
                val userResult = profileUseCase.getUser(travel.ownerId)
                val currentUserId = authRepository.getCurrentUser()?.uid
                val isOwner = travel.ownerId == currentUserId
                val canEdit = isOwner || travel.privileges?.contains(currentUserId) == true
                
                _uiState.update { it.copy(
                    travel = travel,
                    pointsOfInterest = pointsResult.getOrDefault(emptyList()),
                    creatorUser = userResult.getOrNull(),
                    isOwner = isOwner,
                    canEdit = canEdit,
                    isLoading = false
                ) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
