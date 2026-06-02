package com.catedra.bitacora.core.domain.repository

import com.catedra.bitacora.core.domain.model.PointOnMap

interface NearbyPointsRepository {
    suspend fun getNearbyPoints(
        userId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<PointOnMap>>
}
