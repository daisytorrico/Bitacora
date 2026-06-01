package com.catedra.bitacora.features.travel.presentation.travelCreate

import android.net.Uri
import com.catedra.bitacora.features.travel.domain.model.TravelVisibility

data class CreateTravelUiState(
    val name: String = "",
    val description: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val imageUri: Uri? = null,
    val visibility: TravelVisibility = TravelVisibility.PRIVATE,
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val travelId: String? = null
) {
    // Validaciones derivadas del estado
    val isDateInvalid: Boolean
        get() = if (startDate != null && endDate != null) endDate < startDate else false

    val canSave: Boolean
        get() = name.isNotBlank() && 
                startDate != null && 
                endDate != null && 
                !isDateInvalid && 
                !loading
}
