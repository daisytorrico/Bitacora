package com.catedra.bitacora.features.travel.data.mapper

import com.catedra.bitacora.features.travel.domain.model.Travel
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

fun QuerySnapshot.toDomain(): List<Travel> {
    return documents.mapNotNull { document ->
        try {
            val firestoreStartDate = document.getTimestamp("startDate")
                ?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()

            val firestoreEndDate = document.getTimestamp("endDate")
                ?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()

            Travel(
                id = document.id,
                name = document.getString("name") ?: "Viaje sin nombre",
                ownerId = document.getString("ownerId") ?: "",
                description = document.getString("description") ?: "",
                imageUrl = document.getString("imageUrl"),
                startDate = firestoreStartDate,
                endDate = firestoreEndDate,
                privileges = document.get("privileges") as? Map<String, String>
            )
        } catch (e: Exception) {
            null
        }
    }
}

fun Travel.toData(): Map<String, Any?> {
    return hashMapOf(
        "name" to name,
        "description" to description,
        "ownerId" to ownerId,
        "imageUrl" to imageUrl,
        "startDate" to startDate?.toDate(),
        "endDate" to endDate?.toDate(),
        "privileges" to privileges
    )
}

// Helpers privados para conversión de fechas
private fun LocalDate.toDate(): Date {
    return Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())
}
