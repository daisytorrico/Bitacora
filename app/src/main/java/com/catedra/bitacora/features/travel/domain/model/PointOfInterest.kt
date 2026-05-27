package com.catedra.bitacora.features.travel.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class PointOfInterest(
    val id: String = "",
    val name: String,
    val address: String, // Dirección legible (Calle 123...)
    val latitude: Double? = null,
    val longitude: Double? = null,
    val visitDate: LocalDateTime?,
    val notes: String = "",
    val imageUrls: List<String> = emptyList()
)
