package com.catedra.bitacora.core.components.map

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.catedra.bitacora.core.domain.model.PointOnMap
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapComponent(
    viewModel: MapViewModel,
    onPointSelected: (PointOnMap) -> Unit,
    buttonText: String = "Seleccionar",
    modifier: Modifier = Modifier
) {
    MapContent(
        uiState = viewModel.uiState,
        onMapClick = viewModel::onMapClick,
        onClearSelection = viewModel::clearSelection,
        onPointSelected = onPointSelected,
        buttonText = buttonText,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapContent(
    uiState: MapViewUiState,
    onMapClick: (Double, Double) -> Unit,
    onClearSelection: () -> Unit,
    onPointSelected: (PointOnMap) -> Unit,
    buttonText: String,
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

                    locationOverlay.runOnFirstFix {
                        val myLocation = locationOverlay.myLocation
                        post {
                            controller.animateTo(myLocation)
                            controller.setZoom(18.0)
                            invalidate()
                        }
                    }

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
            }
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        uiState.selectedPoint?.let { point ->
            PointDetailCard(
                point = point,
                buttonText = buttonText,
                onCancel = onClearSelection,
                onConfirm = { onPointSelected(point) },
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
