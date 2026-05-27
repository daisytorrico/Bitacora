package com.catedra.bitacora.features.travel.presentation.pointCreate

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.travel.data.mapper.toData
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import com.catedra.bitacora.features.travel.domain.useCase.CompressImageUseCase
import com.catedra.bitacora.features.travel.domain.useCase.GetPhotoPickerIntentUseCase
import com.catedra.bitacora.features.travel.domain.useCase.UploadImageUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.catedra.bitacora.features.travel.domain.useCase.GetCurrentLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CreatePointViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val application: Application,
    private val firestore: FirebaseFirestore,
    private val travelsRepository: TravelsRepository,
    private val getPhotoPickerIntentUseCase: GetPhotoPickerIntentUseCase,
    private val compressImageUseCase: CompressImageUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
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
                    visitDate = uiState.value.visitDateMillis?.toLocalDate(),
                    notes = uiState.value.notes,
                    imageUrls = imageUrls
                )

                firestore.collection("trips")
                    .document(travelId)
                    .collection("pointsOfInterest")
                    .add(point.toData())
                    .await()

                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC")).toLocalDate()
    }

    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }
}
