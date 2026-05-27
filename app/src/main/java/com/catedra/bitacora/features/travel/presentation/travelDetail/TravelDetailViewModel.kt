package com.catedra.bitacora.features.travel.presentation.travelDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TravelDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val travelsRepository: TravelsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val travelId: String = checkNotNull(savedStateHandle["travelId"])

    private val _uiState = MutableStateFlow(TravelDetailUiState())
    val uiState: StateFlow<TravelDetailUiState> = _uiState.asStateFlow()

    init {
        loadTravelDetails()
    }

    fun loadTravelDetails() {
        viewModelScope.launch {
            if (uiState.value.travel == null) {
                _uiState.update { it.copy(isLoading = true) }
            }
            
            val travelResult = travelsRepository.getTravelById(travelId)
            
            travelResult.onSuccess { travel ->
                val pointsResult = travelsRepository.getPointsOfInterest(travelId)
                
                authRepository.getFullUserData().onSuccess { user ->
                    val points = pointsResult.getOrDefault(emptyList())
                    _uiState.update { state ->
                        state.copy(
                            travel = travel.copy(pointsCount = points.size),
                            creatorUser = user,
                            pointsOfInterest = points,
                            isLoading = false
                        )
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
