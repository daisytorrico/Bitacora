package com.catedra.bitacora.features.travel.domain.model

import java.time.LocalDate

data class Travel(
    val id: String,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)
