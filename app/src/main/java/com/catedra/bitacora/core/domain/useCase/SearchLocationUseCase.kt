package com.catedra.bitacora.core.domain.useCase

import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.domain.repository.GeocodingRepository
import javax.inject.Inject

class SearchLocationUseCase @Inject constructor(
    private val repository: GeocodingRepository
) {
    suspend operator fun invoke(query: String): Result<List<PointOnMap>> {
        if (query.isBlank()) return Result.success(emptyList())
        return repository.searchLocation(query)
    }
}
