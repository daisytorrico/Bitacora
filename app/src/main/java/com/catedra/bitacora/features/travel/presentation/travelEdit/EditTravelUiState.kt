package com.catedra.bitacora.features.travel.presentation.travelEdit

import android.net.Uri
import com.catedra.bitacora.features.travel.domain.model.TravelVisibility

data class EditTravelUiState(
    val travelId: String = "",
    val name: String = "",
    val description: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val imageUri: Uri? = null,
    val imageUrl: String? = null,
    val visibility: TravelVisibility = TravelVisibility.PRIVATE,
    val isLoading: Boolean = false,
    val isInitialLoading: Boolean = true,
    val showConfirmDialog: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
) {
    val isDateInvalid: Boolean
        get() = if (startDate != null && endDate != null) endDate < startDate else false

    val canSave: Boolean
        get() = name.isNotBlank() && 
                startDate != null && 
                endDate != null && 
                !isDateInvalid && 
                !isLoading
}
