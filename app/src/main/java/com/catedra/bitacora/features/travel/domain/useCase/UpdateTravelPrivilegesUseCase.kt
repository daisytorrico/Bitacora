package com.catedra.bitacora.features.travel.domain.useCase

import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class UpdateTravelPrivilegesUseCase @Inject constructor(
    private val travelsRepository: TravelsRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(travelId: String, privileges: List<String>): Result<Unit> {
        val currentUserId = authRepository.getCurrentUser()?.uid
            ?: return Result.failure(Exception("No autenticado"))
        val travel = travelsRepository.getTravelById(travelId).getOrNull()
            ?: return Result.failure(Exception("Viaje no encontrado"))

        if (travel.ownerId != currentUserId) {
            return Result.failure(Exception("Sólo el dueño puede gestionar privilegios"))
        }

        val previousPrivileges = travel.privileges ?: emptyList()
        val removed = previousPrivileges.filter { it !in privileges }

        travelsRepository.updateTravel(travel.copy(privileges = privileges)).onFailure {
            return Result.failure(it)
        }

        travelsRepository.syncTripAccess(travelId, privileges, removed)

        return Result.success(Unit)
    }
}