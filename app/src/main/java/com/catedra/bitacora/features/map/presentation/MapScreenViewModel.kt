package com.catedra.bitacora.features.map.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.domain.useCase.GetNearbyPointsUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapScreenViewModel @Inject constructor(
    private val getNearbyPointsUseCase: GetNearbyPointsUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    var uiState by mutableStateOf<List<PointOnMap>>(emptyList())
        private set

    private var fetchJob: Job? = null

    fun onCameraMoved(center: Coordinates) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            delay(500) // Debounce para no saturar Firestore
            loadPoints(center)
        }
    }

    private suspend fun loadPoints(center: Coordinates) {
        val userId = auth.currentUser?.uid ?: return

        // Usamos un radio de 10km para la búsqueda inicial
        getNearbyPointsUseCase(userId, center.latitude, center.longitude, radiusKm = 10.0)
            .onSuccess { points ->
                uiState = points
            }
            .onFailure { exception ->
            }
    }
}
