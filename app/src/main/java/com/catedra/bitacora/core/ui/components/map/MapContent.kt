package com.catedra.bitacora.core.ui.components.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap
import kotlin.math.abs
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun rememberMapState(): MapState {
    val context = LocalContext.current
    return remember {
        val mapView = MapView(context)
        val marker = Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        MapState(mapView, marker)
    }
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
    buttonText: String?,
    onExternalPoiAction: (PointOnMap) -> Unit,
    externalPoiButtonText: String?,
    modifier: Modifier = Modifier,
    showSearch: Boolean = true,
    showControls: Boolean = true,
    isInteractive: Boolean = true,
    showSelectionCard: Boolean = true
) {
    val context = LocalContext.current
    val mapState = rememberMapState()
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
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { _ ->
            mapState.mapView.apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(isInteractive)

                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                locationOverlay.enableMyLocation()
                overlays.add(locationOverlay)

                uiState.cameraCenter?.let { savedCenter ->
                    controller.setCenter(GeoPoint(savedCenter.latitude, savedCenter.longitude))
                    controller.setZoom(uiState.cameraZoom)
                } ?: run {
                    controller.setCenter(GeoPoint(0.0, 0.0))
                    controller.setZoom(10.0)

                    locationOverlay.runOnFirstFix {
                        val myLocation = locationOverlay.myLocation
                        // Solo centrar automáticamente si NO se ha establecido un centro en la UI (por initialPoint por ej)
                        if (myLocation != null && uiState.cameraCenter == null) {
                            post {
                                controller.animateTo(myLocation)
                                controller.setZoom(18.0)
                                invalidate()
                            }
                        }
                    }
                }

                if (isInteractive) {
                    val mapListener = object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            val center = mapCenter
                            if (uiState.cameraCenter?.latitude != center.latitude || 
                                uiState.cameraCenter?.longitude != center.longitude) {
                                onCameraMoved(
                                    Coordinates(center.latitude, center.longitude), zoomLevelDouble
                                )
                            }
                            return true
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean {
                            val center = mapCenter
                            onCameraMoved(
                                Coordinates(center.latitude, center.longitude), zoomLevelDouble
                            )
                            return true
                        }
                    }
                    addMapListener(DelayedMapListener(mapListener, 500))

                    val mapEventsReceiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p?.let { onMapClick(it.latitude, it.longitude) }
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    }
                    overlays.add(MapEventsOverlay(mapEventsReceiver))
                }

                post {
                    onMapReady()
                }
            }
        }, update = {
            mapState.updateSelection(uiState.selectedPoint, uiState.temporaryCoordinates)
            mapState.updateExternalPois(uiState.externalPois, onExternalPoiClicked)
            
            // Sincronizar cámara desde el estado si cambió significativamente (ej: por setInitialPoint)
            uiState.cameraCenter?.let { center ->
                val currentMapCenter = mapState.mapView.mapCenter
                val latDiff = kotlin.math.abs(currentMapCenter.latitude - center.latitude)
                val lonDiff = kotlin.math.abs(currentMapCenter.longitude - center.longitude)
                val zoomDiff = kotlin.math.abs(mapState.mapView.zoomLevelDouble - uiState.cameraZoom)

                // Usamos un umbral pequeño para evitar bucles infinitos por errores de precisión
                if (latDiff > 0.00001 || lonDiff > 0.00001 || zoomDiff > 0.1) {
                    mapState.mapView.controller.setCenter(GeoPoint(center.latitude, center.longitude))
                    mapState.mapView.controller.setZoom(uiState.cameraZoom)
                }
            }
        })

        if (!uiState.isMapReady) {
            Surface(
                modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
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
            if (showSearch) {
                var searchActive by remember { mutableStateOf(false) }

                SearchBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (searchActive) 0.dp else 16.dp, vertical = 4.dp)
                        .align(Alignment.TopCenter),
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChanged,
                    onSearch = { searchActive = false },
                    active = searchActive,
                    onActiveChange = { searchActive = it },
                    placeholder = { Text("Buscar lugar...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                onClearSelection()
                                if (!searchActive) onSearchQueryChanged("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    }) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.searchResults) { point ->
                            ListItem(
                                headlineContent = { Text(point.name) },
                                supportingContent = { Text(point.address) },
                                modifier = Modifier.clickable {
                                    onSearchResultSelected(point)
                                    mapState.animateTo(point.coordinates)
                                    searchActive = false
                                })
                        }
                    }
                }
            }

            if (uiState.isLoading || uiState.isSearching) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (showSelectionCard) {
                uiState.selectedPoint?.let { point ->
                    PointDetailCard(
                        point = point,
                        buttonText = buttonText,
                        onCancel = onClearSelection,
                        onConfirm = { onPointSelected(point) },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }

                uiState.selectedExternalPoi?.let { poi ->
                    PointDetailCard(
                        point = poi,
                        buttonText = externalPoiButtonText,
                        onCancel = onClearSelection,
                        onConfirm = { onExternalPoiAction(poi) },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            if (showControls) {
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            bottom = if (showSelectionCard && (uiState.selectedPoint != null || uiState.selectedExternalPoi != null)) 200.dp else 16.dp,
                            end = 16.dp
                        ),
                    onClick = {
                        val locationOverlay = mapState.mapView.overlays
                            .filterIsInstance<MyLocationNewOverlay>()
                            .firstOrNull()
                        locationOverlay?.myLocation?.let {
                            mapState.mapView.controller.animateTo(it)
                            mapState.mapView.controller.setZoom(18.0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Centrar en mi ubicación")
                }
            }

            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter), action = {
                        TextButton(onClick = onClearSelection) {
                            Text("OK")
                        }
                    }) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
fun PointDetailCard(
    point: PointOnMap,
    buttonText: String?,
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
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) { Text("Cancelar") }
                if (buttonText != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onConfirm) { Text(buttonText) }
                }
            }
        }
    }
}
