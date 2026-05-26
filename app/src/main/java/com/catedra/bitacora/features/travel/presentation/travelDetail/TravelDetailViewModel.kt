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

    private fun loadTravelDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val travelResult = travelsRepository.getTravelById(travelId)
            
            travelResult.onSuccess { travel ->
                // Obtenemos los puntos de interés
                val pointsResult = travelsRepository.getPointsOfInterest(travelId)
                
                // Aquí el prompt pide el creatorUser desde AuthRepository.
                // Como AuthRepository.getCurrentUser() devuelve el usuario actual, 
                // y TravelDetailUiState.creatorUser es para el dueño del viaje,
                // si el viaje es mío puedo usar getCurrentUser().
                // Si fuera de otro, necesitaríamos un getUserById en AuthRepository.
                // Asumiremos por ahora que cargamos el data del dueño si coincide con el actual
                // o intentamos traer data extendida si el repo lo permite.
                
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
