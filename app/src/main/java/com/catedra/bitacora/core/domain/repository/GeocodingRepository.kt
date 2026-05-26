package com.catedra.bitacora.core.domain.repository

import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap

interface GeocodingRepository {
    suspend fun getPointFromCoordinates(coordinates: Coordinates): Result<PointOnMap>
}
