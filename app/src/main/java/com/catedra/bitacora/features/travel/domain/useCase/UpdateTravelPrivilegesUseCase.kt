package com.catedra.bitacora.features.travel.domain.useCase

import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class UpdateTravelPrivilegesUseCase @Inject constructor(
    private val travelsRepository: TravelsRepository
) {
    suspend operator fun invoke(travelId: String, privileges: List<String>): Result<Unit> {
        val travelResult = travelsRepository.getTravelById(travelId)
        val travel = travelResult.getOrNull() ?: return Result.failure(Exception("Viaje no encontrado"))
        
        return travelsRepository.updateTravel(travel.copy(privileges = privileges))
    }
}
