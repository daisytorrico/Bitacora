package com.catedra.bitacora.features.travel.presentation.pointCreate

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import com.catedra.bitacora.core.domain.useCase.CompressImageUseCase
import com.catedra.bitacora.core.domain.useCase.GetPhotoPickerIntentUseCase
import com.catedra.bitacora.core.domain.useCase.UploadImageUseCase
import com.catedra.bitacora.features.travel.domain.useCase.GetCurrentLocationUseCase
import com.catedra.bitacora.features.travel.domain.useCase.SavePointUseCase
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
class CreatePointViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val travelsRepository: TravelsRepository,
    private val getPhotoPickerIntentUseCase: GetPhotoPickerIntentUseCase,
    private val compressImageUseCase: CompressImageUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val savePointUseCase: SavePointUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase
) : ViewModel() {

    private val travelId: String = checkNotNull(savedStateHandle["travelId"])

    private val _uiState = MutableStateFlow(CreatePointUiState())
    val uiState: StateFlow<CreatePointUiState> = _uiState.asStateFlow()

    private var currentTempCameraUri: Uri? = null

    init {
        loadTravelData()
    }

    private fun loadTravelData() {
        viewModelScope.launch {
            travelsRepository.getTravelById(travelId).onSuccess { travel ->
                _uiState.update { it.copy(travel = travel) }
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }
    fun onAddressChange(address: String) = _uiState.update { it.copy(address = address) }
    fun onNotesChange(notes: String) = _uiState.update { it.copy(notes = notes) }
    fun onDateSelected(millis: Long?) = _uiState.update { it.copy(visitDateMillis = millis) }
    fun onTimeSelected(hour: Int, minute: Int) = _uiState.update { it.copy(visitHour = hour, visitMinute = minute) }

    fun onImageAdded(uri: Uri) {
        _uiState.update { it.copy(selectedImages = it.selectedImages + uri) }
    }

    fun onImagesAdded(uris: List<Uri>) {
        _uiState.update { it.copy(selectedImages = it.selectedImages + uris) }
    }

    fun onImageRemoved(uri: Uri) {
        _uiState.update { it.copy(selectedImages = it.selectedImages - uri) }
    }

    fun buildPhotoPickerIntent(): Intent {
        val (intent, tempUri) = getPhotoPickerIntentUseCase(
            title = "Seleccionar fotos del lugar",
            allowMultiple = true
        )
        currentTempCameraUri = tempUri
        return intent
    }

    fun getActiveTempUri(): Uri? = currentTempCameraUri

    @SuppressLint("MissingPermission")
    fun obtenerUbicacionActual() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getCurrentLocationUseCase()
                .onSuccess { userLocation ->
                    _uiState.update { state -> 
                        state.copy(
                            address = userLocation.address, 
                            latitude = userLocation.latitude,
                            longitude = userLocation.longitude,
                            isLoading = false
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }

    fun savePoint() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val imageUrls = mutableListOf<String>()
                for (uri in uiState.value.selectedImages) {
                    val compressedUri = compressImageUseCase(uri)
                    if (compressedUri != null) {
                        val url = uploadImageUseCase(compressedUri)
                        imageUrls.add(url)
                    }
                }

                val point = PointOfInterest(
                    name = uiState.value.name,
                    address = uiState.value.address,
                    latitude = uiState.value.latitude,
                    longitude = uiState.value.longitude,
                    visitDate = uiState.value.visitDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        val hour = uiState.value.visitHour ?: 0
                        val minute = uiState.value.visitMinute ?: 0
                        date.atTime(hour, minute)
                    },
                    notes = uiState.value.notes,
                    imageUrls = imageUrls
                )

                val result = savePointUseCase(travelId, point)

                if (result.isSuccess) {
                    val id = result.getOrNull()
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, pointId = id) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Error al guardar") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }
}
