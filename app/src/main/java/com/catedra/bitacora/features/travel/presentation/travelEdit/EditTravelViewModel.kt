package com.catedra.bitacora.features.travel.presentation.travelEdit

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.core.domain.useCase.CompressImageUseCase
import com.catedra.bitacora.core.domain.useCase.UploadImageUseCase
import com.catedra.bitacora.core.ui.util.PhotoPickerHelper
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.model.TravelVisibility
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import com.catedra.bitacora.features.travel.domain.useCase.UpdateTravelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class EditTravelViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val travelsRepository: TravelsRepository,
    private val updateTravelUseCase: UpdateTravelUseCase,
    private val compressImageUseCase: CompressImageUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val photoPickerHelper: PhotoPickerHelper
) : ViewModel() {

    private val travelId: String = checkNotNull(savedStateHandle["travelId"])

    private val _uiState = MutableStateFlow(EditTravelUiState(travelId = travelId))
    val uiState: StateFlow<EditTravelUiState> = _uiState.asStateFlow()

    private var currentTempCameraUri: Uri? = null

    init {
        loadTravel()
    }

    private fun loadTravel() {
        viewModelScope.launch {
            travelsRepository.getTravelById(travelId).onSuccess { travel ->
                _uiState.update { it.copy(
                    name = travel.name,
                    description = travel.description,
                    startDate = travel.startDate?.atStartOfDay(ZoneId.of("UTC"))?.toInstant()?.toEpochMilli(),
                    endDate = travel.endDate?.atStartOfDay(ZoneId.of("UTC"))?.toInstant()?.toEpochMilli(),
                    imageUrl = travel.imageUrl,
                    visibility = travel.visibility,
                    isInitialLoading = false
                ) }
                // Guardamos el viaje original para no perder datos como ownerId o privileges
                originalTravel = travel
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isInitialLoading = false) }
            }
        }
    }

    private var originalTravel: Travel? = null

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }
    fun onDescriptionChange(desc: String) = _uiState.update { it.copy(description = desc) }
    fun onStartDateSelected(millis: Long?) = _uiState.update { it.copy(startDate = millis) }
    fun onEndDateSelected(millis: Long?) = _uiState.update { it.copy(endDate = millis) }
    fun onVisibilityChange(visibility: TravelVisibility) = _uiState.update { it.copy(visibility = visibility) }
    fun onImageSelected(uri: Uri?) = _uiState.update { it.copy(imageUri = uri) }

    fun buildSystemChooserIntent(): android.content.Intent {
        val (intent, tempUri) = photoPickerHelper("Selecciona una foto o usa la cámara")
        currentTempCameraUri = tempUri
        return intent
    }

    fun getActiveTempUri(): Uri? = currentTempCameraUri

    fun setShowConfirmDialog(show: Boolean) {
        _uiState.update { it.copy(showConfirmDialog = show) }
    }

    fun updateTravel() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConfirmDialog = false) }
            
            try {
                var finalImageUrl = state.imageUrl
                state.imageUri?.let { uri ->
                    compressImageUseCase(uri)?.let { compressed ->
                        finalImageUrl = uploadImageUseCase(compressed)
                    }
                }

                val updatedTravel = originalTravel?.copy(
                    name = state.name,
                    description = state.description,
                    startDate = state.startDate?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate() },
                    endDate = state.endDate?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate() },
                    visibility = state.visibility,
                    imageUrl = finalImageUrl
                ) ?: return@launch

                updateTravelUseCase(updatedTravel).onSuccess {
                    _uiState.update { it.copy(isLoading = false, success = true) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun resetError() = _uiState.update { it.copy(error = null) }
}
