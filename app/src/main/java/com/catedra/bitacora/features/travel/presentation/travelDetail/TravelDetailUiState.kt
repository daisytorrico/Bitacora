package com.catedra.bitacora.features.travel.presentation.travelDetail

import com.catedra.bitacora.features.auth.domain.model.User
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest

data class TravelDetailUiState(
    val isLoading: Boolean = true,
    val travel: Travel? = null,
    val creatorUser: User? = null,
    val pointsOfInterest: List<PointOfInterest> = emptyList(),
    val isOwner: Boolean = false,
    val error: String? = null
)
