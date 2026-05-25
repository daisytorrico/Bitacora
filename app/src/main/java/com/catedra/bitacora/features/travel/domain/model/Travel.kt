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
    val privileges: Map<String, String>? = null
)
