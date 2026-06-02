package com.catedra.bitacora.features.discovery.presentation.explorer

import com.catedra.bitacora.features.travel.domain.model.Travel

data class ExplorerUiState(
    val isLoading: Boolean = false,
    val publicTravels: List<Travel> = emptyList(),
    val followingTravels: List<Travel> = emptyList(),
    val isFollowingLastPage: Boolean = false,
    val isLoadingMoreFollowing: Boolean = false,
    val error: String? = null,
    val lastFollowingDoc: Any? = null
)
