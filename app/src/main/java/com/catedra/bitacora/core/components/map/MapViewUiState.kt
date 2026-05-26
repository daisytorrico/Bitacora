package com.catedra.bitacora.core.components.map

import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap

data class MapViewUiState(
    val selectedPoint: PointOnMap? = null,
    val temporaryCoordinates: Coordinates? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<PointOnMap> = emptyList(),
    val isSearching: Boolean = false,
    val isMapReady: Boolean = false,
    val cameraCenter: Coordinates? = null,
    val cameraZoom: Double = 18.0,
    val externalPois: List<PointOnMap> = emptyList(),
    val selectedExternalPoi: PointOnMap? = null
)
