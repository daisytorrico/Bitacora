package com.catedra.bitacora.features.map.domain.useCase

import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.features.map.domain.repository.MapRepository
import javax.inject.Inject

class GetNearbyPointsUseCase @Inject constructor(
    private val repository: MapRepository
) {
    suspend operator fun invoke(
        userId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0
    ): Result<List<PointOnMap>> = repository.getNearbyPoints(userId, latitude, longitude, radiusKm)
}
