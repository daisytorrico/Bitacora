package com.catedra.bitacora.features.travel.domain.useCase

import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetTravelsListUseCase @Inject constructor(
    private val travelRepository: TravelsRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(page: Int): Result<List<Travel>> = coroutineScope {
        val userId = authRepository.getCurrentUser()?.uid 
            ?: return@coroutineScope Result.failure(Exception("Usuario no autenticado"))
            
        val travelsResult = travelRepository.getTravels(userId, page)
        
        travelsResult.map { travels ->
            travels.map { travel ->
                async {
                    val countResult = travelRepository.getPointsCount(travel.id)
                    travel.copy(pointsCount = countResult.getOrDefault(0))
                }
            }.awaitAll()
        }
    }
}
