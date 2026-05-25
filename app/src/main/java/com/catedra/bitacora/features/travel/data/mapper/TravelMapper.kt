package com.catedra.bitacora.features.travel.data.mapper

import com.catedra.bitacora.features.travel.domain.model.Travel
import com.google.firebase.firestore.QuerySnapshot
import java.time.ZoneId

fun QuerySnapshot.toDomain(): List<Travel> {
    return documents.mapNotNull { document ->
        try {
            Travel(
                id = document.id,
                name = document.getString("name") ?: "",
                startDate = document.getTimestamp("startDate")
                    ?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() 
                    ?: java.time.LocalDate.now(),
                endDate = document.getTimestamp("endDate")
                    ?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() 
                    ?: java.time.LocalDate.now()
            )
        } catch (e: Exception) {
            null
        }
    }
}
