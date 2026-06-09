package com.catedra.bitacora.features.travel.domain.useCase

import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class DeleteTravelUseCase @Inject constructor(
    private val repository: TravelsRepository
) {
    suspend operator fun invoke(travelId: String): Result<Unit> {
        return repository.deleteTravel(travelId)
    }
}
