package com.catedra.bitacora.core.domain.useCase

import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.domain.repository.GeocodingRepository
import javax.inject.Inject

class GetPointFromCoordinatesUseCase @Inject constructor(
    private val repository: GeocodingRepository
) {
    suspend operator fun invoke(coordinates: Coordinates): Result<PointOnMap> {
        return repository.getPointFromCoordinates(coordinates)
    }
}
