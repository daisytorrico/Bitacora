package com.catedra.bitacora.features.travel.presentation.travelCreate

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.useCase.CompressImageUseCase
import com.catedra.bitacora.features.travel.domain.useCase.GetPhotoPickerIntentUseCase
import com.catedra.bitacora.features.travel.domain.useCase.SaveTravelUseCase
import com.catedra.bitacora.features.travel.domain.useCase.UploadImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class CreateTravelViewModel @Inject constructor(
    private val getPhotoPickerIntentUseCase: GetPhotoPickerIntentUseCase,
    private val compressImageUseCase: CompressImageUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val saveTravelUseCase: SaveTravelUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTravelUiState())
    val uiState: StateFlow<CreateTravelUiState> = _uiState.asStateFlow()

    private var currentTempCameraUri: Uri? = null

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onDescriptionChange(newDesc: String) {
        _uiState.update { it.copy(description = newDesc) }
    }

    fun onStartDateSelected(millis: Long?) {
        _uiState.update { it.copy(startDate = millis) }
    }

    fun onEndDateSelected(millis: Long?) {
        _uiState.update { it.copy(endDate = millis) }
    }

    fun formatMillisToDate(millis: Long?): String {
        if (millis == null) return ""
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC") // Evita que reste un día
        return formatter.format(Date(millis))
    }

    fun buildSystemChooserIntent(): Intent {
        val (intent, tempUri) = getPhotoPickerIntentUseCase("Selecciona una foto o usa la cámara")
        currentTempCameraUri = tempUri
        return intent
    }

    fun getActiveTempUri(): Uri? = currentTempCameraUri

    fun saveTravel() {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: return
        val currentData = uiState.value
        
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            
            try {
                // 1. Comprimir imagen si existe
                var finalImageUrl: String? = null
                currentData.imageUri?.let { uri ->
                    val compressedUri = compressImageUseCase(uri)
                    if (compressedUri != null) {
                        // 2. Subir a Cloudinary
                        finalImageUrl = uploadImageUseCase(compressedUri)
                    }
                }

                // 3. Crear el modelo de Viaje
                val newTravel = Travel(
                    name = currentData.name,
                    description = currentData.description,
                    ownerId = currentUserId,
                    imageUrl = finalImageUrl,
                    startDate = currentData.startDate?.toLocalDate(),
                    endDate = currentData.endDate?.toLocalDate()
                )

                // 4. Guardar en Firestore
                val result = saveTravelUseCase(newTravel)
                
                if (result.isSuccess) {
                    val id = result.getOrNull()
                    _uiState.update { it.copy(loading = false, success = true, travelId = id) }
                } else {
                    _uiState.update { it.copy(loading = false, error = result.exceptionOrNull()?.message ?: "Error al guardar") }
                }
                
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = e.message ?: "Error desconocido") }
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
