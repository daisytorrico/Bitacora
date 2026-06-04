package com.catedra.bitacora.features.travel.presentation.pointEdit

import android.net.Uri
import com.catedra.bitacora.features.travel.domain.model.Travel
import java.time.Instant
import java.time.ZoneId

data class EditPointUiState(
    val travel: Travel? = null,
    val pointId: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val visitDateMillis: Long? = null,
    val visitHour: Int? = null,
    val visitMinute: Int? = null,
    val notes: String = "",
    val selectedImages: List<Uri> = emptyList(),
    val remoteImageUrls: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isInitialLoading: Boolean = true,
    val isSuccess: Boolean = false,
    val showConfirmDialog: Boolean = false,
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
