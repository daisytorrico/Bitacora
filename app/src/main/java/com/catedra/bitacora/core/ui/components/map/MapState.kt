package com.catedra.bitacora.core.ui.components.map

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.domain.model.Coordinates

class MapState(
    val mapView: MapView,
    private val selectionMarker: Marker
) {
    private val poiMarkers = mutableListOf<Marker>()

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
                selectionMarker.alpha = 0.5f
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

    fun updateExternalPois(pois: List<PointOnMap>, onPoiClicked: (PointOnMap) -> Unit) {
        // Eliminar marcadores antiguos
        poiMarkers.forEach { mapView.overlays.remove(it) }
        poiMarkers.clear()

        // Crear nuevos marcadores
        pois.forEach { poi ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(poi.coordinates.latitude, poi.coordinates.longitude)
                title = poi.name
                subDescription = poi.address
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { m, _ ->
                    onPoiClicked(poi)
                    m.showInfoWindow()
                    true
                }
            }
            poiMarkers.add(marker)
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    fun animateTo(coordinates: Coordinates) {
        mapView.controller.animateTo(GeoPoint(coordinates.latitude, coordinates.longitude))
        mapView.controller.setZoom(18.0)
    }
}
