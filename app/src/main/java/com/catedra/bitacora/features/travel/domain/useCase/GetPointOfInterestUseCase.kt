package com.catedra.bitacora.features.travel.domain.useCase

import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class GetPointOfInterestUseCase @Inject constructor(
    private val repository: TravelsRepository
) {
    suspend operator fun invoke(travelId: String, pointId: String): Result<PointOfInterest> {
        return repository.getPointOfInterest(travelId, pointId)
    }
}
