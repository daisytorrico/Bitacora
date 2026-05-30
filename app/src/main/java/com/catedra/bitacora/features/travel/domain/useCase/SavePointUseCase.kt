package com.catedra.bitacora.features.travel.domain.useCase

import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class SavePointUseCase @Inject constructor(
    private val travelsRepository: TravelsRepository
) {
    suspend operator fun invoke(travelId: String, point: PointOfInterest): Result<String> {
        return travelsRepository.savePoint(travelId, point)
    }
}