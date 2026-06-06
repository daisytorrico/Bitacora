package com.catedra.bitacora.features.discovery.domain.useCase

import com.catedra.bitacora.features.discovery.domain.repository.DiscoveryRepository
import com.catedra.bitacora.features.travel.domain.model.Travel
import javax.inject.Inject

class SearchTravelsUseCase @Inject constructor(
    private val repository: DiscoveryRepository
) {
    suspend operator fun invoke(query: String): Result<List<Travel>> =
        repository.searchTravels(query)
}