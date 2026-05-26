package com.catedra.bitacora.features.travel.presentation.travelDetail

import com.catedra.bitacora.features.auth.domain.model.User
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest

data class TravelDetailUiState(
    val isLoading: Boolean = true,
    val travel: Travel? = null,
    val creatorUser: User? = null, // Datos del dueño del viaje (nombre, @username, foto)
    val pointsOfInterest: List<PointOfInterest> = emptyList(), // Puntos reales de Firestore
    val error: String? = null
)
