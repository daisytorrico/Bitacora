package com.catedra.bitacora.features.travel.presentation.travelList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.auth.domain.useCase.GetFullUserDataUseCase
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
    private val getTravelsList: GetTravelsListUseCase,
    private val getFullUserDataUseCase: GetFullUserDataUseCase,
    private val authRepository: AuthRepository
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
            val currentUserId = authRepository.getCurrentUser()?.uid
            getTravelsList(
                page = uiState.value.page
            ).onSuccess { data ->
                val myTravels = data.filter { it.ownerId == currentUserId }
                val sharedTravels = data.filter { it.ownerId != currentUserId }
                
                _uiState.update { it.copy(
                    myTravels = myTravels,
                    sharedTravels = sharedTravels,
                    loading = false
                ) }
            }.onFailure {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }
}
