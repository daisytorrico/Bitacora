package com.catedra.bitacora.features.travel.data.mapper

import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.model.TravelVisibility
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

fun QuerySnapshot.toDomain(): List<Travel> {
    return documents.toDomain()
}

fun List<DocumentSnapshot>.toDomain(): List<Travel> {
    return this.mapNotNull { it.toTravel() }
}

fun DocumentSnapshot.toTravel(): Travel? {
    return try {
        val firestoreStartDate = getTimestamp("startDate")
            ?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()

        val firestoreEndDate = getTimestamp("endDate")
            ?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()

        val updatedAt = getTimestamp("updatedAt")
            ?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()

        Travel(
            id = id,
            name = getString("name") ?: "Viaje sin nombre",
            ownerId = getString("ownerId") ?: "",
            description = getString("description") ?: "",
            imageUrl = getString("imageUrl"),
            startDate = firestoreStartDate,
            endDate = firestoreEndDate,
            pointsCount = getLong("pointsCount")?.toInt() ?: 0,
            durationDays = getLong("durationDays")?.toInt() ?: 0,
            visibility = getString("visibility")?.uppercase()?.let { TravelVisibility.valueOf(it) } ?: TravelVisibility.PRIVATE,
            privileges = get("privileges") as? List<String>,
            updatedAt = updatedAt
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
        "visibility" to visibility.name.lowercase(),
        "privileges" to (privileges ?: emptyList<String>()),
        "pointsCount" to pointsCount,
        "durationDays" to if (startDate != null && endDate != null) {
            ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        } else 0
    ).filterValues { it != null }
}

private fun LocalDate.toDate(): Date {
    return Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())
}
