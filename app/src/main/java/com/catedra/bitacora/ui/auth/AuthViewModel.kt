package com.catedra.bitacora.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    private val _authState = MutableStateFlow<AuthState>(AuthState.Cargando)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user == null) {
            _authState.value = AuthState.NoAutenticado
        } else {
            // Verificamos perfil si el usuario acaba de entrar o si estamos en proceso de carga
            val current = _authState.value
            if (current !is AuthState.Autenticado && 
                current !is AuthState.NecesitaPerfil &&
                current !is AuthState.EsperandoUsername) {
                verificarPerfil(user.uid)
            }
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    fun estaLogueado(): Boolean = auth.currentUser != null

    private fun verificarPerfil(uid: String) {
        viewModelScope.launch {
            try {
                val document = db.collection("users").document(uid).get().await()
                if (document.exists() && document.contains("username")) {
                    _authState.value = AuthState.Autenticado
                } else {
                    _authState.value = AuthState.NecesitaPerfil
                }
            } catch (e: Exception) {
                Log.e("Auth", "Error al verificar perfil", e)
                _authState.value = AuthState.Error("Error de base de datos")
            }
        }
    }

    fun guardarUsername(username: String) {
        val user = auth.currentUser ?: return
        _authState.value = AuthState.Cargando
        _usernameError.value = null
        
        viewModelScope.launch {
            try {
                // Verificación de unicidad
                val query = db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .await()

                if (!query.isEmpty) {
                    _usernameError.value = "El nombre de usuario ya está en uso"
                    _authState.value = AuthState.EsperandoUsername
                    return@launch
                }

                val datos = hashMapOf(
                    "nombre" to user.displayName,
                    "email" to user.email,
                    "username" to username
                )
                db.collection("users").document(user.uid).set(datos).await()
                _authState.value = AuthState.Autenticado
            } catch (e: Exception) {
                Log.e("Auth", "Error al guardar username", e)
                _usernameError.value = "Error al conectar con la base de datos"
                _authState.value = AuthState.EsperandoUsername
            }
        }
    }

    fun registrar(nombre: String, email: String, pass: String) {
        _authState.value = AuthState.Cargando
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = result.user
                val profileUpdates = userProfileChangeRequest {
                    displayName = nombre
                }
                user?.updateProfile(profileUpdates)?.await()
                user?.reload()?.await()
                
                if (user != null) {
                    verificarPerfil(user.uid)
                }
            } catch (e: Exception) {
                Log.e("Auth", "Error en registro", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Error al registrarse")
            }
        }
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
        _authState.value = AuthState.NoAutenticado
    }

    fun limpiarUsernameError() {
        _usernameError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}
