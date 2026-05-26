package com.catedra.bitacora.features.map.domain.repository

import com.catedra.bitacora.features.map.domain.model.PointOnMap

interface GeocodingRepository {
    suspend fun getPointFromCoordinates(latitude: Double, longitude: Double): Result<PointOnMap>
}
