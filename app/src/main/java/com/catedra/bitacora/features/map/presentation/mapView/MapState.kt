package com.catedra.bitacora.features.map.presentation.mapView

import android.content.Context
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint
import com.catedra.bitacora.features.map.domain.model.PointOnMap
import com.catedra.bitacora.core.domain.model.Coordinates

class MapState(
    val mapView: MapView,
    private val selectionMarker: Marker
) {
    fun updateSelection(point: PointOnMap?, temporaryCoordinates: Coordinates?) {
        when {
            point != null -> {
                val geoPoint = GeoPoint(point.coordinates.latitude, point.coordinates.longitude)
                selectionMarker.position = geoPoint
                selectionMarker.title = point.name
                selectionMarker.alpha = 1.0f
                if (!mapView.overlays.contains(selectionMarker)) {
                    mapView.overlays.add(selectionMarker)
                }
            }
            temporaryCoordinates != null -> {
                val geoPoint = GeoPoint(temporaryCoordinates.latitude, temporaryCoordinates.longitude)
                selectionMarker.position = geoPoint
                selectionMarker.title = "Cargando..."
                selectionMarker.alpha = 0.5f // Feedback visual de carga
                if (!mapView.overlays.contains(selectionMarker)) {
                    mapView.overlays.add(selectionMarker)
                }
            }
            else -> {
                mapView.overlays.remove(selectionMarker)
            }
        }
        mapView.invalidate()
    }
}
