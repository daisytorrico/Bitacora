package com.catedra.bitacora.core.ui.components.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.R
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.domain.useCase.GetPointFromCoordinatesUseCase
import com.catedra.bitacora.core.domain.useCase.SearchLocationUseCase
import com.catedra.bitacora.core.ui.util.UiText
import com.catedra.bitacora.features.travel.domain.useCase.CheckLocationEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getPointFromCoordinatesUseCase: GetPointFromCoordinatesUseCase,
    private val searchLocationUseCase: SearchLocationUseCase,
    private val checkLocationEnabledUseCase: CheckLocationEnabledUseCase
) : ViewModel() {

    var uiState by mutableStateOf(MapViewUiState())
        private set

    private val _searchQueryFlow = MutableStateFlow("")
    
    private var geocodeJob: Job? = null

    init {
        observeSearchQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQueryFlow
                .debounce(500)
                .distinctUntilChanged()
                .filter { it.length > 2 }
                .collectLatest { query ->
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
                }
        }
    }

    fun isGpsEnabled(): Boolean = checkLocationEnabledUseCase()

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
                        error = exception.message?.let { UiText.DynamicString(it) } 
                            ?: UiText.StringResource(R.string.unknown_err),
                        isLoading = false,
                        temporaryCoordinates = null
                    )
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(searchQuery = query)
        if (query.length <= 2) {
            uiState = uiState.copy(searchResults = emptyList())
        }
        _searchQueryFlow.value = query
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

    fun setInitialPoint(point: PointOnMap) {
        uiState = uiState.copy(
            selectedPoint = point,
            cameraCenter = point.coordinates,
            cameraZoom = 18.0
        )
    }

    fun onCameraMoved(center: Coordinates, zoom: Double) {
        uiState = uiState.copy(
            cameraCenter = center,
            cameraZoom = zoom
        )
    }
}
