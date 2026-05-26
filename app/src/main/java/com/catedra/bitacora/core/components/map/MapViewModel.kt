package com.catedra.bitacora.core.components.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.domain.useCase.GetPointFromCoordinatesUseCase
import com.catedra.bitacora.core.domain.useCase.SearchLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getPointFromCoordinatesUseCase: GetPointFromCoordinatesUseCase,
    private val searchLocationUseCase: SearchLocationUseCase
) : ViewModel() {

    var uiState by mutableStateOf(MapViewUiState())
        private set

    private var searchJob: Job? = null
    private var geocodeJob: Job? = null

    fun onMapClick(latitude: Double, longitude: Double) {
        val coordinates = Coordinates(latitude, longitude)
        
        uiState = uiState.copy(
            temporaryCoordinates = coordinates,
            selectedPoint = null,
            selectedExternalPoi = null,
            error = null,
            searchResults = emptyList()
        )

        geocodeJob?.cancel()
        geocodeJob = viewModelScope.launch {
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

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(searchQuery = query)
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.length > 2) {
                delay(500)
                uiState = uiState.copy(isSearching = true)
                searchLocationUseCase(query)
                    .onSuccess { results ->
                        uiState = uiState.copy(
                            searchResults = results,
                            isSearching = false
                        )
                    }
                    .onFailure { 
                        uiState = uiState.copy(isSearching = false)
                    }
            } else {
                uiState = uiState.copy(searchResults = emptyList())
            }
        }
    }

    fun onSearchResultSelected(point: PointOnMap) {
        uiState = uiState.copy(
            selectedPoint = point,
            selectedExternalPoi = null,
            searchQuery = point.name,
            searchResults = emptyList(),
            temporaryCoordinates = null
        )
    }

    fun onExternalPoiClicked(poi: PointOnMap) {
        uiState = uiState.copy(
            selectedExternalPoi = poi,
            selectedPoint = null,
            temporaryCoordinates = null,
            searchResults = emptyList()
        )
    }

    fun setExternalPois(pois: List<PointOnMap>) {
        uiState = uiState.copy(externalPois = pois)
    }

    fun clearSelection() {
        uiState = uiState.copy(
            selectedPoint = null,
            selectedExternalPoi = null,
            temporaryCoordinates = null,
            error = null,
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    fun setMapReady(ready: Boolean) {
        uiState = uiState.copy(isMapReady = ready)
    }

    fun onCameraMoved(center: Coordinates, zoom: Double) {
        uiState = uiState.copy(
            cameraCenter = center,
            cameraZoom = zoom
        )
    }
}
