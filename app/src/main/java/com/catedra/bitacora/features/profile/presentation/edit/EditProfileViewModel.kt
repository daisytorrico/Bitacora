package com.catedra.bitacora.features.profile.presentation.edit

import android.net.Uri
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.R
import com.catedra.bitacora.core.domain.repository.SessionRepository
import com.catedra.bitacora.features.profile.domain.useCase.UpdateProfileUseCase
import com.catedra.bitacora.core.domain.useCase.CompressImageUseCase
import com.catedra.bitacora.core.helpers.PhotoPickerHelper
import com.catedra.bitacora.core.domain.useCase.UploadImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val name: String = "",
    val bio: String = "",
    val photoUrl: String? = null,
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val photoPickerHelper: PhotoPickerHelper,
    private val compressImageUseCase: CompressImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var currentTempCameraUri: Uri? = null

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            sessionRepository.getFullUserData().onSuccess { user ->
                _uiState.update { it.copy(
                    name = user.displayName ?: "",
                    bio = user.bio ?: "",
                    photoUrl = user.photoUrl
                ) }
            }
        }
    }

    fun buildSystemChooserIntent(title: String): Intent {
        val (intent, tempUri) = photoPickerHelper(title)
        currentTempCameraUri = tempUri
        return intent
    }

    fun getActiveTempUri(): Uri? = currentTempCameraUri

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onBioChange(newBio: String) {
        _uiState.update { it.copy(bio = newBio) }
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                var finalPhotoUrl = uiState.value.photoUrl
                
                uiState.value.selectedImageUri?.let { uri ->
                    val compressedUri = compressImageUseCase(uri)
                    if (compressedUri != null) {
                        finalPhotoUrl = uploadImageUseCase(compressedUri)
                    }
                }

                updateProfileUseCase(
                    name = uiState.value.name,
                    bio = uiState.value.bio,
                    photoUrl = finalPhotoUrl
                ).onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
