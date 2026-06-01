package com.catedra.bitacora.core.components.map

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.utils.LocationPermissionHandler

@Composable
fun MapComponent(
    modifier: Modifier = Modifier,
    buttonText: String? = null,
    onPointSelected: (PointOnMap) -> Unit = {},
    externalPois: List<PointOnMap> = emptyList(),
    onExternalPoiSelected: (PointOnMap) -> Unit = {},
    externalPoiButtonText: String? = null,
    onCameraMoved: (Coordinates) -> Unit = {},
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LocationPermissionHandler { isGranted ->
        if (isGranted && !viewModel.isGpsEnabled()) {
            Toast.makeText(context, "El GPS está desactivado. Actívalo para ver tu ubicación real.", Toast.LENGTH_LONG).show()
        }
    }

    // Sincronizar POIs externos con el ViewModel
    LaunchedEffect(externalPois) {
        viewModel.setExternalPois(externalPois)
    }

    MapContent(
        uiState = viewModel.uiState,
        onMapClick = viewModel::onMapClick,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSearchResultSelected = viewModel::onSearchResultSelected,
        onExternalPoiClicked = viewModel::onExternalPoiClicked,
        onClearSelection = viewModel::clearSelection,
        onMapReady = { viewModel.setMapReady(true) },
        onCameraMoved = { center, zoom ->
            viewModel.onCameraMoved(center, zoom)
            onCameraMoved(center)
        },
        onPointSelected = onPointSelected,
        buttonText = buttonText,
        onExternalPoiAction = onExternalPoiSelected,
        externalPoiButtonText = externalPoiButtonText,
        modifier = modifier
    )
}
