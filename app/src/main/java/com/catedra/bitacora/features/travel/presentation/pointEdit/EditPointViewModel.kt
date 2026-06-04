package com.catedra.bitacora.features.travel.presentation.pointEdit

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.core.domain.useCase.CompressImageUseCase
import com.catedra.bitacora.core.domain.useCase.UploadImageUseCase
import com.catedra.bitacora.core.ui.util.PhotoPickerHelper
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import com.catedra.bitacora.features.travel.domain.useCase.UpdatePointUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class EditPointViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val travelsRepository: TravelsRepository,
    private val updatePointUseCase: UpdatePointUseCase,
    private val compressImageUseCase: CompressImageUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val photoPickerHelper: PhotoPickerHelper
) : ViewModel() {

    private val travelId: String = checkNotNull(savedStateHandle["travelId"])
    private val pointId: String = checkNotNull(savedStateHandle["pointId"])

    private val _uiState = MutableStateFlow(EditPointUiState(pointId = pointId))
    val uiState: StateFlow<EditPointUiState> = _uiState.asStateFlow()

    private var currentTempCameraUri: Uri? = null

    init {
        loadPoint()
    }

    private fun loadPoint() {
        viewModelScope.launch {
            val travelResult = travelsRepository.getTravelById(travelId)
            val pointResult = travelsRepository.getPointOfInterest(travelId, pointId)

            if (travelResult.isSuccess && pointResult.isSuccess) {
                val travel = travelResult.getOrNull()
                val point = pointResult.getOrNull()!!
                _uiState.update { it.copy(
                    travel = travel,
                    name = point.name,
                    address = point.address,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    visitDateMillis = point.visitDate?.atZone(ZoneId.of("UTC"))?.toInstant()?.toEpochMilli(),
                    visitHour = point.visitDate?.hour,
                    visitMinute = point.visitDate?.minute,
                    notes = point.notes,
                    remoteImageUrls = point.imageUrls,
                    isInitialLoading = false
                ) }
            } else {
                _uiState.update { it.copy(
                    isInitialLoading = false,
                    error = "Error al cargar los datos"
                ) }
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }
    fun onAddressChange(address: String) = _uiState.update { it.copy(address = address) }
    fun onDateSelected(millis: Long?) = _uiState.update { it.copy(visitDateMillis = millis) }
    fun onTimeSelected(hour: Int, minute: Int) = _uiState.update { it.copy(visitHour = hour, visitMinute = minute) }
    fun onNotesChange(notes: String) = _uiState.update { it.copy(notes = notes) }
    fun onImageAdded(uri: Uri) = _uiState.update { it.copy(selectedImages = it.selectedImages + uri) }
    fun onImagesAdded(uris: List<Uri>) = _uiState.update { it.copy(selectedImages = it.selectedImages + uris) }
    fun onImageRemoved(uri: Uri) = _uiState.update { it.copy(selectedImages = it.selectedImages - uri) }
    fun onRemoteImageRemoved(url: String) = _uiState.update { it.copy(remoteImageUrls = it.remoteImageUrls - url) }
    fun onToggleMap(show: Boolean) = _uiState.update { it.copy(showMapSelector = show) }

    fun buildPhotoPickerIntent(): android.content.Intent {
        val (intent, tempUri) = photoPickerHelper("Selecciona una foto o usa la cámara")
        currentTempCameraUri = tempUri
        return intent
    }

    fun getActiveTempUri(): Uri? = currentTempCameraUri

    fun setShowConfirmDialog(show: Boolean) {
        _uiState.update { it.copy(showConfirmDialog = show) }
    }

    fun updatePoint() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConfirmDialog = false) }
            
            try {
                val newRemoteUrls = state.selectedImages.mapNotNull { uri ->
                    compressImageUseCase(uri)?.let { compressed ->
                        uploadImageUseCase(compressed)
                    }
                }

                val visitDateTime = if (state.visitDateMillis != null) {
                    val date = Instant.ofEpochMilli(state.visitDateMillis).atZone(ZoneId.of("UTC")).toLocalDate()
                    val time = LocalTime.of(state.visitHour ?: 0, state.visitMinute ?: 0)
                    LocalDateTime.of(date, time)
                } else null

                val updatedPoint = PointOfInterest(
                    id = pointId,
                    name = state.name,
                    address = state.address,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    visitDate = visitDateTime,
                    notes = state.notes,
                    imageUrls = state.remoteImageUrls + newRemoteUrls
                )

                updatePointUseCase(travelId, updatedPoint).onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
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
