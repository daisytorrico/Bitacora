package com.catedra.bitacora.features.map.data.mapper

import android.location.Address
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.features.map.domain.model.PointOnMap

fun Address.toDomain(coordinates: Coordinates): PointOnMap {
    val name = featureName ?: getAddressLine(0) ?: "Punto seleccionado"
    return PointOnMap(
        name = name,
        coordinates = coordinates
    )
}
