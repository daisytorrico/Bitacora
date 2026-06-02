package com.catedra.bitacora.core.domain.model

sealed class AuthState {
    object Cargando : AuthState()
    object Autenticado : AuthState()
    object NecesitaPerfil : AuthState()
    object EsperandoUsername : AuthState()
    object NoAutenticado : AuthState()
    data class Error(val mensaje: String) : AuthState()
}
