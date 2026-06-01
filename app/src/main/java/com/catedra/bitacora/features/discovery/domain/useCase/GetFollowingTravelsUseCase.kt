package com.catedra.bitacora.features.discovery.domain.useCase

import com.catedra.bitacora.features.discovery.domain.model.TravelPage
import com.catedra.bitacora.features.discovery.domain.repository.DiscoveryRepository
import javax.inject.Inject

class GetFollowingTravelsUseCase @Inject constructor(
    private val repository: DiscoveryRepository
) {
    suspend operator fun invoke(
        limit: Long = 10,
        lastDocument: Any? = null
    ): Result<TravelPage> = 
        repository.getFollowingTravels(limit, lastDocument)
}
