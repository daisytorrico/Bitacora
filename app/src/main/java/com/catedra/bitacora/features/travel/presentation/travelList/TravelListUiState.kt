package com.catedra.bitacora.features.travel.presentation.travelList

import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.features.travel.domain.model.Travel

data class TravelListUiState(
    val user: User? = null,
    val searchQuery: String = "",
    val myTravels: List<Travel> = emptyList(),
    val sharedTravels: List<Travel> = emptyList(),
    val page: Int = 0,
    val loading: Boolean = true
) {
    val filteredMyTravels: List<Travel>
        get() = if (searchQuery.isBlank()) {
            myTravels
        } else {
            myTravels.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

    val filteredSharedTravels: List<Travel>
        get() = if (searchQuery.isBlank()) {
            sharedTravels
        } else {
            sharedTravels.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
}
