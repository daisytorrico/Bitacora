package com.catedra.bitacora.core.domain.useCase

import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.domain.repository.NearbyPointsRepository
import javax.inject.Inject

class GetNearbyPointsUseCase @Inject constructor(
    private val repository: NearbyPointsRepository
) {
    suspend operator fun invoke(
        userId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0
    ): Result<List<PointOnMap>> = repository.getNearbyPoints(userId, latitude, longitude, radiusKm)
}
