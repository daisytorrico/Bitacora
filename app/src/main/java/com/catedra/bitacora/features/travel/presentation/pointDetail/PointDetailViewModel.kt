package com.catedra.bitacora.features.travel.presentation.pointDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.travel.domain.useCase.GetPointOfInterestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PointDetailViewModel @Inject constructor(
    private val getPointOfInterestUseCase: GetPointOfInterestUseCase,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val travelId: String = checkNotNull(savedStateHandle["travelId"])
    private val pointId: String = checkNotNull(savedStateHandle["pointId"])

    private val _uiState = MutableStateFlow(PointDetailUiState())
    val uiState: StateFlow<PointDetailUiState> = _uiState.asStateFlow()

    init {
        loadPointDetail()
    }

    private fun loadPointDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val pointResult = getPointOfInterestUseCase(travelId, pointId)
            val userResult = authRepository.getFullUserData()

            if (pointResult.isSuccess) {
                _uiState.update { it.copy(
                    isLoading = false, 
                    point = pointResult.getOrNull(),
                    creatorUser = userResult.getOrNull(),
                    error = null
                ) }
            } else {
                _uiState.update { it.copy(
                    isLoading = false, 
                    error = pointResult.exceptionOrNull()?.message 
                ) }
            }
        }
    }

    fun onToggleMap(show: Boolean) {
        _uiState.update { it.copy(showMap = show) }
    }
}
