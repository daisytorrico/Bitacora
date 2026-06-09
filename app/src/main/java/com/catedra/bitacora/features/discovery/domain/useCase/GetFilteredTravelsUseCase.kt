package com.catedra.bitacora.features.discovery.domain.useCase

import com.catedra.bitacora.features.discovery.domain.model.TravelPage
import com.catedra.bitacora.features.discovery.domain.repository.DiscoveryRepository
import com.catedra.bitacora.features.discovery.presentation.explorer.DurationFilter
import com.catedra.bitacora.features.travel.domain.model.Travel
import javax.inject.Inject

class GetFilteredTravelsUseCase @Inject constructor(
    private val repository: DiscoveryRepository
) {
    suspend operator fun invoke(
        limit: Long = 10,
        lastDocument: Any? = null,
        searchQuery: String? = null,
        durationFilter: DurationFilter? = null,
        isDetailedOnly: Boolean = false,
        selectedMonth: Int? = null,
        selectedYear: Int? = null
    ): Result<TravelPage> = repository.getFilteredTravels(
        limit = limit,
        lastDocument = lastDocument,
        searchQuery = searchQuery,
        durationFilter = durationFilter,
        isDetailedOnly = isDetailedOnly,
        selectedMonth = selectedMonth,
        selectedYear = selectedYear
    )
}