package com.catedra.bitacora.features.travel.presentation.pointDetail

import com.catedra.bitacora.features.auth.domain.model.User
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest

data class PointDetailUiState(
    val isLoading: Boolean = true,
    val point: PointOfInterest? = null,
    val creatorUser: User? = null,
    val error: String? = null
)
