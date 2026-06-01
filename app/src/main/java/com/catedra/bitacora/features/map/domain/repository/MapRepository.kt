package com.catedra.bitacora.features.map.domain.repository

import com.catedra.bitacora.core.domain.model.PointOnMap

interface MapRepository {
    suspend fun getNearbyPoints(
        userId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<PointOnMap>>
}
