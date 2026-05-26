package com.catedra.bitacora.features.map.domain.useCase

import com.catedra.bitacora.features.map.domain.model.PointOnMap
import com.catedra.bitacora.features.map.domain.repository.GeocodingRepository
import javax.inject.Inject

class GetPointFromCoordinatesUseCase @Inject constructor(
    private val repository: GeocodingRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double): Result<PointOnMap> {
        return repository.getPointFromCoordinates(latitude, longitude)
    }
}
