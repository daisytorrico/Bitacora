package com.catedra.bitacora.features.travel.presentation.travelDetail

import com.catedra.bitacora.features.travel.domain.model.Travel

data class TravelDetailUiState(
    val searchQuery: String = "",
    val travels: List<Travel> = emptyList(),
    val page: Int = 0,
    val loading: Boolean = true
) {
    val filteredTravels: List<Travel>
        get() = if (searchQuery.isBlank()) {
            travels
        } else {
            travels.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
}
