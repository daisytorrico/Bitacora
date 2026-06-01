package com.catedra.bitacora.features.discovery.domain.useCase

import com.catedra.bitacora.features.discovery.domain.repository.DiscoveryRepository
import javax.inject.Inject

class GetFollowingIdsUseCase @Inject constructor(
    private val repository: DiscoveryRepository
) {
    suspend operator fun invoke(): Result<List<String>> = 
        repository.getFollowingIds()
}
