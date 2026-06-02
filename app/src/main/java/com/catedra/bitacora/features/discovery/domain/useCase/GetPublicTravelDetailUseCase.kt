package com.catedra.bitacora.features.discovery.domain.useCase

import com.catedra.bitacora.features.discovery.domain.repository.DiscoveryRepository
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.catedra.bitacora.features.travel.domain.model.Travel
import javax.inject.Inject

class GetPublicTravelDetailUseCase @Inject constructor(
    private val repository: DiscoveryRepository
) {
    suspend fun getTravel(travelId: String): Result<Travel> = repository.getPublicTravelDetail(travelId)
    suspend fun getPoints(travelId: String): Result<List<PointOfInterest>> = repository.getPublicPointsOfInterest(travelId)
}
