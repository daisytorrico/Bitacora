package com.catedra.bitacora.features.travel.domain.model

import java.time.LocalDate

data class Travel(
    val id: String = "",
    val name: String,
    val description: String = "",
    val ownerId: String,
    val imageUrl: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val pointsCount: Int = 0,
    val privileges: Map<String, String>? = null
) {
    val status: TravelStatus
        get() {
            val today = LocalDate.now()
            return when {
                startDate == null || endDate == null -> TravelStatus.PLANNED
                today.isBefore(startDate) -> TravelStatus.PLANNED
                today.isAfter(endDate) -> TravelStatus.COMPLETED
                else -> TravelStatus.ONGOING
            }
        }
}
