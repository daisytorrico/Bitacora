package com.catedra.bitacora.features.discovery.presentation.explorer

import com.catedra.bitacora.core.ui.util.UiText
import com.catedra.bitacora.features.travel.domain.model.Travel

data class ExplorerUiState(
    val isLoading: Boolean = false,
    val publicTravels: List<Travel> = emptyList(),
    val followingTravels: List<Travel> = emptyList(),
    val isFollowingLastPage: Boolean = false,
    val isLoadingMoreFollowing: Boolean = false,
    val error: UiText? = null,
    val lastFollowingDoc: Any? = null,
    val searchQuery: String = "",
    val searchResults: List<Travel> = emptyList(),
    val isSearching: Boolean = false,
    val isSearchModeActive: Boolean = false,
    val selectedDuration: DurationFilter? = null,
    val isDetailedOnly: Boolean = false,
    val selectedMonth: Int? = null,
    val selectedYear: Int? = null,
    val filterResults: List<Travel> = emptyList(),
    val isFilterModeActive: Boolean = false,
    val filterLastDocument: Any? = null,
    val isFilterLastPage: Boolean = false,
    val isLoadingMoreFiltered: Boolean = false
)

enum class DurationFilter {
    SHORT,
    MEDIUM,
    LONG
}
fun ExplorerUiState.hasActiveFilter() = selectedDuration != null || isDetailedOnly || selectedMonth != null