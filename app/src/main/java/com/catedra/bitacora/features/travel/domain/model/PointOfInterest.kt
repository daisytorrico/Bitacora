package com.catedra.bitacora.features.travel.domain.model

import java.time.LocalDate

data class PointOfInterest(
    val id: String = "",
    val name: String,
    val address: String, // Dirección legible (Calle 123...)
    val latitude: Double? = null,
    val longitude: Double? = null,
    val visitDate: LocalDate?,
    val notes: String = "",
    val imageUrls: List<String> = emptyList()
)
