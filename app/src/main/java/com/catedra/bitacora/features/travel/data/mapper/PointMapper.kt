package com.catedra.bitacora.features.travel.data.mapper

import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import java.time.ZoneId
import java.util.Date

fun PointOfInterest.toData(): Map<String, Any?> {
    return hashMapOf(
        "name" to name,
        "address" to address,
        "location" to if (latitude != null && longitude != null) GeoPoint(latitude, longitude) else null,
        "notes" to notes,
        "visitDate" to visitDate?.let {
            val date = Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())
            Timestamp(date)
        },
        "imageUrls" to imageUrls
    )
}

fun DocumentSnapshot.toPointOfInterest(): PointOfInterest {
    val geoPoint = getGeoPoint("location")
    return PointOfInterest(
        id = id,
        name = getString("name") ?: "",
        address = getString("address") ?: "",
        latitude = geoPoint?.latitude,
        longitude = geoPoint?.longitude,
        visitDate = getTimestamp("visitDate")?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())?.toLocalDate(),
        notes = getString("notes") ?: "",
        imageUrls = get("imageUrls") as? List<String> ?: emptyList()
    )
}

fun QuerySnapshot.toPointsDomain(): List<PointOfInterest> {
    return documents.map { it.toPointOfInterest() }
}
