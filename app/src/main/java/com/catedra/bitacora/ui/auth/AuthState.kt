package com.catedra.bitacora.ui.auth

sealed class AuthState {
    object Cargando : AuthState()
    object Autenticado : AuthState()
    object NoAutenticado : AuthState()
    data class Error(val mensaje: String) : AuthState()
}
