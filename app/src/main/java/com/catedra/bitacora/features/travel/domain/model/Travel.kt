package com.catedra.bitacora.features.travel.domain.model

import java.time.LocalDate

/**
 * Representa un viaje (Trip) en el sistema.
 * Basado en las reglas de seguridad de Firestore.
 */
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
