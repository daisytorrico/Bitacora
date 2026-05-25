package com.catedra.bitacora.features.travel.presentation.travelList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.travel.domain.useCase.GetTravelsListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TravelListViewModel @Inject constructor(
    private val getTravelsList: GetTravelsListUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelListUiState())
    val uiState: StateFlow<TravelListUiState> = _uiState.asStateFlow()

    init {
        loadTravels()
    }

    fun loadTravels() {
        viewModelScope.launch {
            getTravelsList(
                page = uiState.value.page
            ).onSuccess { data ->
                _uiState.update { it.copy(travels = data, loading = false) }
            }.onFailure {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }
}
