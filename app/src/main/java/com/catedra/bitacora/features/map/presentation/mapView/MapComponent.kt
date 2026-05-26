package com.catedra.bitacora.features.map.presentation.mapView

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapView(viewModel: MapViewModel) {
    Scaffold { paddingValues ->
        AndroidView(
            modifier = Modifier.padding(paddingValues),
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)

                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    locationOverlay.enableMyLocation()
                    locationOverlay.enableFollowLocation()
                    overlays.add(locationOverlay)


                }
            },
            update = { mapView ->
            }
        )
    }
}