package com.catedra.bitacora.features.travel.domain.useCase

import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class GetSharedTravelsUseCase @Inject constructor(
    private val travelRepository: TravelsRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<List<Travel>> {
        val userId = authRepository.getCurrentUser()?.uid
            ?: return Result.failure(Exception("Usuario no autenticado"))

        return travelRepository.getSharedTravels(userId)
    }
}