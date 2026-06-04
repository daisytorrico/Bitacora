package com.catedra.bitacora.features.travel.presentation.privileges

import com.catedra.bitacora.core.domain.model.User

data class ManagePrivilegesUiState(
    val isLoading: Boolean = true,
    val collaborators: List<User> = emptyList(),
    val searchResults: List<User> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)
