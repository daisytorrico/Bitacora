package com.catedra.bitacora.features.travel.presentation.pointCreate

import android.net.Uri
import com.catedra.bitacora.features.travel.domain.model.Travel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class CreatePointUiState(
    val travel: Travel? = null,
    val name: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val visitDateMillis: Long? = null,
    val visitHour: Int? = null,
    val visitMinute: Int? = null,
    val notes: String = "",
    val selectedImages: List<Uri> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val pointId: String? = null,
    val error: String? = null,
    val showMapSelector: Boolean = false
) {
    val isDateOutOfRange: Boolean
        get() {
            if (visitDateMillis == null || travel == null) return false
            val visitDate = Instant.ofEpochMilli(visitDateMillis).atZone(ZoneId.of("UTC")).toLocalDate()
            val start = travel.startDate
            val end = travel.endDate
            return if (start != null && end != null) {
                visitDate.isBefore(start) || visitDate.isAfter(end)
            } else false
        }

    val canSave: Boolean 
        get() = name.isNotBlank() && 
                address.isNotBlank() && 
                !isDateOutOfRange &&
                !isLoading
}
