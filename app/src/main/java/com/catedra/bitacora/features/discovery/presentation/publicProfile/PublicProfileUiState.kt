package com.catedra.bitacora.features.discovery.presentation.publicProfile

import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.presentation.travelList.TravelListUiState

data class PublicProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val travels: List<Travel> = emptyList(),
    val isFollowing: Boolean = false,
    val error: String? = null
) {
    fun toTravelListUiState() = TravelListUiState(
        user = user,
        travels = travels,
        loading = isLoading
    )
}
