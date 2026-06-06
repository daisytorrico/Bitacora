package com.catedra.bitacora.features.discovery.presentation.explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val searchTravelsUseCase: SearchTravelsUseCase

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
                _uiState.update { it.copy(isLoading = false, error = "Error al cargar feed") }
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
        if (query.isBlank()) clearSearch()
    }

    fun performSearch() {
        val query = uiState.value.searchQuery.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, isSearchModeActive = true) }
            searchTravelsUseCase(query)
                .onSuccess { results ->
                    _uiState.update { it.copy(
                        searchResults = results,
                        isSearching = false
                    )}
                }
                .onFailure {
                    _uiState.update { it.copy(
                        isSearching = false,
                        error = "Error al buscar"
                    )}
                }
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(
            searchQuery = "",
            searchResults = emptyList(),
            isSearchModeActive = false,
            isSearching = false
        )}
    }
}
