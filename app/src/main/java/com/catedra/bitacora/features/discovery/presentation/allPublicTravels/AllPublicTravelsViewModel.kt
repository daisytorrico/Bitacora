package com.catedra.bitacora.features.discovery.presentation.allPublicTravels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.discovery.domain.useCase.GetFollowingIdsUseCase
import com.catedra.bitacora.features.discovery.domain.useCase.GetPublicTravelsUseCase
import com.catedra.bitacora.features.travel.domain.model.Travel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllPublicTravelsViewModel @Inject constructor(
    private val getPublicTravelsUseCase: GetPublicTravelsUseCase,
    private val getFollowingIdsUseCase: GetFollowingIdsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AllPublicTravelsUiState())
    val uiState: StateFlow<AllPublicTravelsUiState> = _uiState.asStateFlow()

    init {
        loadMoreTravels()
    }

    fun loadMoreTravels() {
        if (uiState.value.isLoading || uiState.value.isLastPage) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val followingIds = getFollowingIdsUseCase().getOrDefault(emptyList())
            
            val result = getPublicTravelsUseCase(
                limit = 10, 
                lastDocument = uiState.value.lastDocument, 
                excludeOwnerIds = followingIds
            )
            
            result.onSuccess { page ->
                _uiState.update { it.copy(
                    travels = (it.travels + page.travels).distinctBy { t -> t.id },
                    isLoading = false,
                    isLastPage = page.travels.size < 10,
                    lastDocument = page.lastDocument,
                    error = null
                ) }
            }.onFailure { e ->
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message
                ) }
            }
        }
    }

}

data class AllPublicTravelsUiState(
    val isLoading: Boolean = false,
    val travels: List<Travel> = emptyList(),
    val isLastPage: Boolean = false,
    val error: String? = null,
    val lastDocument: Any? = null,
    val searchQuery: String = "",
    val searchResults: List<Travel> = emptyList(),
    val isSearching: Boolean = false,
    val isSearchModeActive: Boolean = false
)
