package com.catedra.bitacora.core.components.map

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap
import org.osmdroid.config.Configuration
import org.osmdroid.events.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapComponent(
    onPointSelected: (PointOnMap) -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String = "Seleccionar",
    externalPois: List<PointOnMap> = emptyList(),
    onExternalPoiSelected: (PointOnMap) -> Unit = {},
    externalPoiButtonText: String = "Detalle",
    viewModel: MapViewModel = hiltViewModel()
) {
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
        onCameraMoved = viewModel::onCameraMoved,
        onPointSelected = onPointSelected,
        buttonText = buttonText,
        onExternalPoiAction = onExternalPoiSelected,
        externalPoiButtonText = externalPoiButtonText,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapContent(
    uiState: MapViewUiState,
    onMapClick: (Double, Double) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSearchResultSelected: (PointOnMap) -> Unit,
    onExternalPoiClicked: (PointOnMap) -> Unit,
    onClearSelection: () -> Unit,
    onMapReady: () -> Unit,
    onCameraMoved: (Coordinates, Double) -> Unit,
    onPointSelected: (PointOnMap) -> Unit,
    buttonText: String,
    onExternalPoiAction: (PointOnMap) -> Unit,
    externalPoiButtonText: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapState = rememberMapState(context)
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapState.mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapState.mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapState.mapView.onDetach()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { _ ->
                mapState.mapView.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    locationOverlay.enableMyLocation()
                    locationOverlay.enableFollowLocation()
                    overlays.add(locationOverlay)

                    // Si ya teníamos una posición (por rotación), la restauramos inmediatamente
                    uiState.cameraCenter?.let { savedCenter ->
                        controller.setCenter(GeoPoint(savedCenter.latitude, savedCenter.longitude))
                        controller.setZoom(uiState.cameraZoom)
                        onMapReady()
                    } ?: run {
                        locationOverlay.runOnFirstFix {
                            val myLocation = locationOverlay.myLocation
                            post {
                                controller.animateTo(myLocation)
                                controller.setZoom(18.0)
                                invalidate()
                                onMapReady()
                            }
                        }
                    }

                    // Escuchamos el movimiento para guardar la posición en el ViewModel
                    val mapListener = object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            val center = mapCenter
                            onCameraMoved(Coordinates(center.latitude, center.longitude), zoomLevelDouble)
                            return true
                        }
                        override fun onZoom(event: ZoomEvent?): Boolean {
                            val center = mapCenter
                            onCameraMoved(Coordinates(center.latitude, center.longitude), zoomLevelDouble)
                            return true
                        }
                    }
                    addMapListener(DelayedMapListener(mapListener, 200))

                    val mapEventsReceiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p?.let { onMapClick(it.latitude, it.longitude) }
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    }
                    overlays.add(MapEventsOverlay(mapEventsReceiver))
                }
            },
            update = {
                mapState.updateSelection(uiState.selectedPoint, uiState.temporaryCoordinates)
                mapState.updateExternalPois(uiState.externalPois, onExternalPoiClicked)
            }
        )

        if (!uiState.isMapReady) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Preparando mapa...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        if (uiState.isMapReady) {
            // Barra de búsqueda
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                DockedSearchBar(
                    modifier = Modifier.fillMaxWidth(),
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChanged,
                    onSearch = { /* Ya se busca con debounce */ },
                    active = uiState.searchResults.isNotEmpty(),
                    onActiveChange = { },
                    placeholder = { Text("Buscar lugar...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = onClearSelection) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    }
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.searchResults) { point ->
                            ListItem(
                                headlineContent = { Text(point.name) },
                                supportingContent = { Text(point.address) },
                                modifier = Modifier.clickable {
                                    onSearchResultSelected(point)
                                    mapState.animateTo(point.coordinates)
                                }
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading || uiState.isSearching) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Tarjeta para punto nuevo seleccionado
            uiState.selectedPoint?.let { point ->
                PointDetailCard(
                    point = point,
                    buttonText = buttonText,
                    onCancel = onClearSelection,
                    onConfirm = { onPointSelected(point) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Tarjeta para POI externo seleccionado
            uiState.selectedExternalPoi?.let { poi ->
                PointDetailCard(
                    point = poi,
                    buttonText = externalPoiButtonText,
                    onCancel = onClearSelection,
                    onConfirm = { onExternalPoiAction(poi) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp).align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = onClearSelection) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
fun PointDetailCard(
    point: PointOnMap,
    buttonText: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = point.name, style = MaterialTheme.typography.titleMedium)
            if (point.address.isNotEmpty()) {
                Text(
                    text = point.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) { Text("Cancelar") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onConfirm) { Text(buttonText) }
            }
        }
    }
}

@Composable
fun rememberMapState(context: Context): MapState {
    return remember {
        Configuration.getInstance().userAgentValue = context.packageName
        val mapView = MapView(context)
        val marker = Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        MapState(mapView, marker)
    }
}
