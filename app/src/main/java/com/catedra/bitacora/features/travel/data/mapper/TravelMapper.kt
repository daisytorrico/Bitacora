package com.catedra.bitacora.features.travel.data.mapper

import com.catedra.bitacora.features.travel.domain.model.Travel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

fun QuerySnapshot.toDomain(): List<Travel> {
    return documents.mapNotNull { it.toTravel() }
}

fun DocumentSnapshot.toTravel(): Travel? {
    return try {
        val firestoreStartDate = getTimestamp("startDate")
            ?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()

        val firestoreEndDate = getTimestamp("endDate")
            ?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()

        Travel(
            id = id,
            name = getString("name") ?: "Viaje sin nombre",
            ownerId = getString("ownerId") ?: "",
            description = getString("description") ?: "",
            imageUrl = getString("imageUrl"),
            startDate = firestoreStartDate,
            endDate = firestoreEndDate,
            pointsCount = getLong("pointsCount")?.toInt() ?: 0,
            privileges = get("privileges") as? Map<String, String>
        )
    } catch (e: Exception) {
        null
    }
}

fun Travel.toData(): Map<String, Any?> {
    return hashMapOf(
        "name" to name,
        "description" to description,
        "ownerId" to ownerId,
        "imageUrl" to imageUrl,
        "startDate" to startDate?.let {
            Timestamp(Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant()))
        },
        "endDate" to endDate?.let {
            Timestamp(Date.from(it.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()))
        },
        "privileges" to privileges
    )
}

// Helpers privados para conversión de fechas
private fun LocalDate.toDate(): Date {
    return Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())
}
