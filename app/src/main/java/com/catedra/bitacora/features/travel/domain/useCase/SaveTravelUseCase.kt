package com.catedra.bitacora.features.travel.domain.useCase

import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class SaveTravelUseCase @Inject constructor(
    private val travelsRepository: TravelsRepository
) {
    suspend operator fun invoke(travel: Travel): Result<Unit> {
        return travelsRepository.saveTravel(travel)
    }
}
