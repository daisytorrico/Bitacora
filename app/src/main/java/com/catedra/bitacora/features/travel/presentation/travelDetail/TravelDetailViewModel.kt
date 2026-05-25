package com.catedra.bitacora.features.travel.presentation.travelDetail

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
class TravelDetailViewModel @Inject constructor(
    private val getTravelsList: GetTravelsListUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelDetailUiState())
    val uiState: StateFlow<TravelDetailUiState> = _uiState.asStateFlow()

    init {
        loadTravels()
    }

    fun loadTravels() {
        viewModelScope.launch {
            getTravelsList(
                page = uiState.value.page
            ).onSuccess { data ->
                _uiState.update { it.copy(travels = data) }
            }.onFailure {
                TODO("Señal de error")
            }
        }
    }
}