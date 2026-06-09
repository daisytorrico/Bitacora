package com.catedra.bitacora.features.auth.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.core.domain.model.AuthState
import com.catedra.bitacora.features.auth.domain.useCase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    getAuthStateUseCase: GetAuthStateUseCase,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val saveUsernameUseCase: SaveUsernameUseCase,
    private val resetAuthErrorUseCase: ResetAuthErrorUseCase
) : ViewModel() {

    val authState: StateFlow<AuthState> = getAuthStateUseCase()
    
    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()
    
    val loginEmail = savedStateHandle.getStateFlow("loginEmail", "")
    val loginPassword = savedStateHandle.getStateFlow("loginPassword", "")
    
    val registerName = savedStateHandle.getStateFlow("registerName", "")
    val registerEmail = savedStateHandle.getStateFlow("registerEmail", "")
    val registerPassword = savedStateHandle.getStateFlow("registerPassword", "")
    val registerConfirmPassword = savedStateHandle.getStateFlow("registerConfirmPassword", "")
    
    val username = savedStateHandle.getStateFlow("username", "")

    fun onLoginEmailChange(value: String) {
        savedStateHandle["loginEmail"] = value
        clearError()
    }

    fun onLoginPasswordChange(value: String) {
        savedStateHandle["loginPassword"] = value
        clearError()
    }

    fun onRegisterNameChange(value: String) {
        savedStateHandle["registerName"] = value
        clearError()
    }

    fun onRegisterEmailChange(value: String) {
        savedStateHandle["registerEmail"] = value
        clearError()
    }

    fun onRegisterPasswordChange(value: String) {
        savedStateHandle["registerPassword"] = value
        clearError()
    }

    fun onRegisterConfirmPasswordChange(value: String) {
        savedStateHandle["registerConfirmPassword"] = value
        clearError()
    }

    fun onUsernameChange(value: String) {
        if (value.length <= 20) {
            savedStateHandle["username"] = value.lowercase().trim()
            clearUsernameError()
        }
    }

    fun saveUsername() {
        _usernameError.value = null
        viewModelScope.launch {
            saveUsernameUseCase(username.value).onFailure { e ->
                _usernameError.value = e.message ?: "Error al guardar username"
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            registerUseCase(registerName.value, registerEmail.value, registerPassword.value).onFailure { _ -> }
        }
    }

    fun login() {
        viewModelScope.launch {
            loginUseCase(loginEmail.value, loginPassword.value).onFailure { _ -> }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            googleSignInUseCase(idToken).onFailure { _ -> }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun clearError() {
        resetAuthErrorUseCase()
    }

    fun clearUsernameError() {
        _usernameError.value = null
    }
}
