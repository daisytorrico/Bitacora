package com.catedra.bitacora.core.data.mapper

import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.domain.model.ExternalPoi
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toPointOnMap(): PointOnMap {
    val geoPoint = getGeoPoint("location")
    val name = getString("name") ?: ""
    val address = getString("address") ?: ""
    val coordinates = Coordinates(
        latitude = geoPoint?.latitude ?: 0.0,
        longitude = geoPoint?.longitude ?: 0.0
    )

    // Si es un documento de la subcolección de puntos de interés, podemos obtener el travelId
    val travelId = reference.parent.parent?.id
    
    return if (travelId != null) {
        ExternalPoi(
            id = id,
            travelId = travelId,
            name = name,
            address = address,
            coordinates = coordinates
        )
    } else {
        PointOnMap(
            name = name,
            address = address,
            coordinates = coordinates
        )
    }
}
