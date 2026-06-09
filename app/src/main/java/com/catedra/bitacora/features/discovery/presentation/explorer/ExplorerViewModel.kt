package com.catedra.bitacora.features.discovery.presentation.explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.R
import com.catedra.bitacora.core.ui.util.UiText
import com.catedra.bitacora.features.discovery.domain.useCase.GetFilteredTravelsUseCase
import com.catedra.bitacora.features.discovery.domain.useCase.GetFollowingIdsUseCase
import com.catedra.bitacora.features.discovery.domain.useCase.GetFollowingTravelsUseCase
import com.catedra.bitacora.features.discovery.domain.useCase.GetPublicTravelsUseCase
import com.catedra.bitacora.features.discovery.domain.useCase.SearchTravelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExplorerViewModel @Inject constructor(
    private val getPublicTravelsUseCase: GetPublicTravelsUseCase,
    private val getFollowingTravelsUseCase: GetFollowingTravelsUseCase,
    private val getFollowingIdsUseCase: GetFollowingIdsUseCase,
    private val getFilteredTravelsUseCase: GetFilteredTravelsUseCase

) : ViewModel() {
    private val _uiState = MutableStateFlow(ExplorerUiState())
    val uiState: StateFlow<ExplorerUiState> = _uiState.asStateFlow()

    init {
        loadDiscoveryData()
    }

    fun loadDiscoveryData(isSilent: Boolean = false) {
        viewModelScope.launch {
            if (!isSilent && uiState.value.publicTravels.isEmpty() && uiState.value.followingTravels.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }
            
            try {
                coroutineScope {
                    val followingIdsDeferred = async { getFollowingIdsUseCase().getOrDefault(emptyList()) }
                    val followingIds = followingIdsDeferred.await()

                    val publicResultDeferred = async { 
                        getPublicTravelsUseCase(limit = 2, lastDocument = null, excludeOwnerIds = followingIds) 
                    }
                    val followingResultDeferred = async { 
                        getFollowingTravelsUseCase(limit = 10, lastDocument = null) 
                    }

                    val publicResult = publicResultDeferred.await()
                    val followingResult = followingResultDeferred.await()

                    _uiState.update { it.copy(
                        publicTravels = publicResult.getOrNull()?.travels ?: emptyList(),
                        followingTravels = followingResult.getOrNull()?.travels ?: emptyList(),
                        lastFollowingDoc = followingResult.getOrNull()?.lastDocument,
                        isFollowingLastPage = (followingResult.getOrNull()?.travels?.size ?: 0) < 10,
                        isLoading = false,
                        error = null
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = UiText.StringResource(R.string.loading_feed_err)) }
            }
        }
    }

    fun loadMoreFollowing() {
        val currentState = uiState.value
        if (currentState.isLoadingMoreFollowing || currentState.isFollowingLastPage) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMoreFollowing = true) }
            
            val result = getFollowingTravelsUseCase(limit = 10, lastDocument = currentState.lastFollowingDoc)
            
            result.onSuccess { page ->
                _uiState.update { it.copy(
                    followingTravels = (it.followingTravels + page.travels).distinctBy { t -> t.id },
                    lastFollowingDoc = page.lastDocument,
                    isFollowingLastPage = page.travels.size < 10,
                    isLoadingMoreFollowing = false
                ) }
            }.onFailure {
                _uiState.update { it.copy(isLoadingMoreFollowing = false) }
            }
        }
    }
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank() && !uiState.value.hasActiveFilter()) {
            clearAll()
        }
    }

    fun performSearch() {
        if (uiState.value.searchQuery.isBlank()) return
        applySearchAndFilters()
    }

    fun onDurationFilterChange(filter: DurationFilter?) {
        val newFilter = if (uiState.value.selectedDuration == filter) null else filter
        _uiState.update { it.copy(
            selectedDuration = newFilter,
            isDetailedOnly = false,
            selectedMonth = null
        )}
        applySearchAndFilters()
    }

    fun onDetailedFilterChange() {
        _uiState.update { it.copy(
            isDetailedOnly = !uiState.value.isDetailedOnly,
            selectedDuration = null,
            selectedMonth = null
        )}
        applySearchAndFilters()
    }

    fun onMonthFilterChange(month: Int?, year: Int?) {
        _uiState.update { it.copy(
            selectedMonth = month,
            selectedYear = year,
            selectedDuration = null,
            isDetailedOnly = false
        )}
        applySearchAndFilters()
    }

    private fun applySearchAndFilters() {
        val state = uiState.value
        val hasText = state.searchQuery.isNotBlank()
        val hasFilter = state.hasActiveFilter()

        if (!hasText && !hasFilter) {
            clearAll()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(
                filterResults = emptyList(),
                isSearching = true,
                isFilterModeActive = true
            )}
            getFilteredTravelsUseCase(
                searchQuery = if (hasText) state.searchQuery.trim() else null,
                durationFilter = state.selectedDuration,
                isDetailedOnly = state.isDetailedOnly,
                selectedMonth = state.selectedMonth,
                selectedYear = state.selectedYear
            ).onSuccess { results ->
                _uiState.update { it.copy(
                    filterResults = results.travels,
                    filterLastDocument = results.lastDocument,
                    isFilterLastPage = results.travels.size < 10,
                    isSearching = false
                )}
            }.onFailure {
                _uiState.update { it.copy(isSearching = false, error = UiText.StringResource(R.string.filter_err)) }
            }
        }
    }

    fun loadMoreFiltered() {
        val state = uiState.value
        if (state.isLoadingMoreFiltered || state.isFilterLastPage) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMoreFiltered = true) }
            getFilteredTravelsUseCase(
                lastDocument = state.filterLastDocument,
                searchQuery = if (state.searchQuery.isNotBlank()) state.searchQuery.trim() else null,
                durationFilter = state.selectedDuration,
                isDetailedOnly = state.isDetailedOnly,
                selectedMonth = state.selectedMonth,
                selectedYear = state.selectedYear
            ).onSuccess { page ->
                _uiState.update { it.copy(
                    filterResults = (it.filterResults + page.travels).distinctBy { t -> t.id },
                    filterLastDocument = page.lastDocument,
                    isFilterLastPage = page.travels.size < 10,
                    isLoadingMoreFiltered = false
                )}
            }.onFailure {
                _uiState.update { it.copy(isLoadingMoreFiltered = false) }
            }
        }
    }
    fun clearAll() {
        _uiState.update { it.copy(
            searchQuery = "",
            searchResults = emptyList(),
            filterResults = emptyList(),
            isSearchModeActive = false,
            isFilterModeActive = false,
            isSearching = false,
            selectedDuration = null,
            isDetailedOnly = false,
            selectedMonth = null,
            selectedYear = null,
            filterLastDocument = null,
            isFilterLastPage = false
        )}
    }

    fun clearSearch() {
        _uiState.update { it.copy(
            searchQuery = "",
            searchResults = emptyList(),
            isSearchModeActive = false,
            isSearching = false
        )}
        if (!uiState.value.hasActiveFilter()) {
            _uiState.update { it.copy(isFilterModeActive = false) }
        } else {
            applySearchAndFilters()
        }
    }
}
