package com.catedra.bitacora.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.auth.domain.model.AuthState
import com.catedra.bitacora.features.auth.domain.useCase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val saveUsernameUseCase: SaveUsernameUseCase
) : ViewModel() {

    val authState: StateFlow<AuthState> = getAuthStateUseCase()
    
    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    fun estaLogueado(): Boolean {
        return authState.value is AuthState.Autenticado
    }

    fun guardarUsername(username: String) {
        _usernameError.value = null
        viewModelScope.launch {
            saveUsernameUseCase(username).onFailure { e ->
                _usernameError.value = e.message ?: "Error al guardar username"
            }
        }
    }

    fun registrar(nombre: String, email: String, pass: String) {
        viewModelScope.launch {
            registerUseCase(nombre, email, pass).onFailure { e ->
                // Opcionalmente podrías manejar un estado de error específico de registro aquí
            }
        }
    }

    fun iniciarSesion(email: String, pass: String) {
        viewModelScope.launch {
            loginUseCase(email, pass).onFailure { e ->
                // Opcionalmente manejar error de login
            }
        }
    }

    fun iniciarSesionConGoogle(idToken: String) {
        viewModelScope.launch {
            googleSignInUseCase(idToken).onFailure { e ->
                // Opcionalmente manejar error de Google
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun limpiarUsernameError() {
        _usernameError.value = null
    }
}
