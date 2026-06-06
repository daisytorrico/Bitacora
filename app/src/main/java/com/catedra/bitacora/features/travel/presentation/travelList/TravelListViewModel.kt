package com.catedra.bitacora.features.travel.presentation.travelList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.auth.domain.useCase.GetFullUserDataUseCase
import com.catedra.bitacora.features.travel.domain.model.TravelStatus
import com.catedra.bitacora.features.travel.domain.model.TravelVisibility
import com.catedra.bitacora.features.travel.domain.useCase.GetSharedTravelsUseCase
import com.catedra.bitacora.features.travel.domain.useCase.GetTravelsListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TravelListViewModel @Inject constructor(
    private val getTravelsList: GetTravelsListUseCase,
    private val getSharedTravels: GetSharedTravelsUseCase,
    private val getFullUserDataUseCase: GetFullUserDataUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelListUiState())
    val uiState: StateFlow<TravelListUiState> = _uiState.asStateFlow()

    fun loadUserData() {
        viewModelScope.launch {
            getFullUserDataUseCase().onSuccess { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun loadTravels() {
        viewModelScope.launch {
            val myTravelsJob = async { getTravelsList(page = uiState.value.page) }
            val sharedTravelsJob = async { getSharedTravels() }

            val myResult = myTravelsJob.await()
            val sharedResult = sharedTravelsJob.await()

            _uiState.update { it.copy(
                myTravels = myResult.getOrDefault(emptyList()),
                sharedTravels = sharedResult.getOrDefault(emptyList()),
                loading = false
            )}
        }
    }

    fun onStatusFilterChange(status: TravelStatus?) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    fun onVisibilityFilterChange(visibility: TravelVisibility?) {
        _uiState.update { it.copy(selectedVisibility = visibility) }
    }
}
