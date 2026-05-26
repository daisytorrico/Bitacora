package com.catedra.bitacora.core.components.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.useCase.GetPointFromCoordinatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getPointFromCoordinatesUseCase: GetPointFromCoordinatesUseCase
) : ViewModel() {

    var uiState by mutableStateOf(MapViewUiState())
        private set

    private var searchJob: Job? = null

    fun onMapClick(latitude: Double, longitude: Double) {
        val coordinates = Coordinates(latitude, longitude)
        
        uiState = uiState.copy(
            temporaryCoordinates = coordinates,
            selectedPoint = null,
            error = null
        )

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            uiState = uiState.copy(isLoading = true)
            getPointFromCoordinatesUseCase(coordinates)
                .onSuccess { point ->
                    uiState = uiState.copy(
                        selectedPoint = point,
                        temporaryCoordinates = null,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    uiState = uiState.copy(
                        error = exception.message ?: "Error desconocido",
                        isLoading = false,
                        temporaryCoordinates = null
                    )
                }
        }
    }

    fun clearSelection() {
        uiState = uiState.copy(
            selectedPoint = null, 
            temporaryCoordinates = null,
            error = null
        )
    }
}
