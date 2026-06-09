package com.catedra.bitacora.features.travel.presentation.pointDetail

import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.catedra.bitacora.features.travel.domain.model.Travel

data class PointDetailUiState(
    val travelId: String = "",
    val pointId: String = "",
    val currentUserId: String? = null,
    val isLoading: Boolean = true,
    val point: PointOfInterest? = null,
    val creatorUser: User? = null,
    val isOwner: Boolean = false,
    val canEdit: Boolean = false,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val error: String? = null,
    val showMap: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleted: Boolean = false,
    val myTravels: List<Travel> = emptyList(),
    val showTripSelector: Boolean = false,
    val isLoadingTrips: Boolean = false
)
