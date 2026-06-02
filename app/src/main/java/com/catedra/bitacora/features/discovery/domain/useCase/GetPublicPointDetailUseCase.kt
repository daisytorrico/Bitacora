package com.catedra.bitacora.features.discovery.domain.useCase

import com.catedra.bitacora.features.discovery.domain.repository.DiscoveryRepository
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import javax.inject.Inject

class GetPublicPointDetailUseCase @Inject constructor(
    private val repository: DiscoveryRepository
) {
    suspend operator fun invoke(travelId: String, pointId: String): Result<PointOfInterest> {
        return repository.getPublicPointDetail(travelId, pointId)
    }
}
