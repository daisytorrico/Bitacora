package com.catedra.bitacora.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _authState = MutableStateFlow<AuthState>(AuthState.Cargando)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        Log.d("AUTH", "AuthStateListener disparado, user: ${firebaseAuth.currentUser?.email}")
        _authState.value = if (firebaseAuth.currentUser != null) {
            AuthState.Autenticado
        } else {
            AuthState.NoAutenticado
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun iniciarSesion(email: String, pass: String) {
        _authState.value = AuthState.Cargando
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun registrar(email: String, pass: String) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, pass).await()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al registrarse")
            }
        }
    }

    fun iniciarSesionConGoogle(idToken: String) {
        _authState.value = AuthState.Cargando
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error con Google")
            }
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }
    fun resetearError() {
        _authState.value = AuthState.NoAutenticado
    }
}