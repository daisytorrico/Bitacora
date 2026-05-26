package com.catedra.bitacora.features.auth.data.repository

import android.util.Log
import com.catedra.bitacora.features.auth.data.mapper.toDomain
import com.catedra.bitacora.features.auth.data.remote.AuthRemoteDataSource
import com.catedra.bitacora.features.auth.domain.model.AuthState
import com.catedra.bitacora.features.auth.domain.model.User
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.auth.data.mapper.toData
import com.catedra.bitacora.features.auth.data.mapper.toUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryFirebase @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Cargando)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user == null) {
            _authState.value = AuthState.NoAutenticado
        } else {
            val current = _authState.value
            if (current !is AuthState.Autenticado &&
                current !is AuthState.NecesitaPerfil &&
                current !is AuthState.EsperandoUsername
            ) {
                verificarPerfil(user.uid)
            }
        }
    }

    init {
        remoteDataSource.addAuthStateListener(authStateListener)
    }

    private fun verificarPerfil(uid: String) {
        repositoryScope.launch {
            try {
                val document = remoteDataSource.getUserDocument(uid)
                if (document.exists() && document.contains("username")) {
                    _authState.value = AuthState.Autenticado
                } else {
                    _authState.value = AuthState.NecesitaPerfil
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error al verificar perfil", e)
                _authState.value = AuthState.Error("Error de base de datos")
            }
        }
    }

    override suspend fun loginWithEmail(email: String, pass: String): Result<Unit> {
        return try {
            remoteDataSource.signInWithEmail(email, pass)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerWithEmail(nombre: String, email: String, pass: String): Result<Unit> {
        return try {
            remoteDataSource.createUserWithEmail(email, pass)
            
            val profileUpdates = userProfileChangeRequest {
                displayName = nombre
            }
            remoteDataSource.updateProfile(profileUpdates)
            remoteDataSource.reloadUser()
            
            val user = remoteDataSource.currentUser
            if (user != null) {
                verificarPerfil(user.uid)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            remoteDataSource.signInWithCredential(credential)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        remoteDataSource.signOut()
        _authState.value = AuthState.NoAutenticado
    }

    override suspend fun saveUsername(username: String): Result<Unit> {
        val user = remoteDataSource.currentUser ?: return Result.failure(Exception("No user logged in"))
        
        return try {
            val isUnique = remoteDataSource.checkUsernameUniqueness(username)
            if (!isUnique) {
                return Result.failure(Exception("El nombre de usuario ya está en uso"))
            }

            val userToSave = User(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName,
                username = username,
                photoUrl = user.photoUrl?.toString(),
                bio = ""
            )
            remoteDataSource.saveUserDocument(user.uid, userToSave.toData())
            _authState.value = AuthState.Autenticado
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFullUserData(): Result<User> {
        val firebaseUser = remoteDataSource.currentUser ?: return Result.failure(Exception("No user logged in"))
        return try {
            val document = remoteDataSource.getUserDocument(firebaseUser.uid)
            if (document.exists()) {
                Result.success(document.toUser())
            } else {
                Result.success(firebaseUser.toDomain())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(name: String, bio: String, photoUrl: String?): Result<Unit> {
        val user = remoteDataSource.currentUser ?: return Result.failure(Exception("No user logged in"))
        return try {
            val profileUpdates = userProfileChangeRequest {
                displayName = name
                photoUrl?.let { this.photoUri = android.net.Uri.parse(it) }
            }
            remoteDataSource.updateProfile(profileUpdates)
            remoteDataSource.reloadUser()

            val updates = mapOf(
                "nombre" to name,
                "bio" to bio,
                "photoUrl" to photoUrl
            )
            
            val currentDoc = remoteDataSource.getUserDocument(user.uid)
            val finalData = currentDoc.data?.toMutableMap() ?: mutableMapOf()
            finalData.putAll(updates)
            remoteDataSource.saveUserDocument(user.uid, finalData)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        return remoteDataSource.currentUser?.toDomain()
    }

    override fun isUserLoggedIn(): Boolean = remoteDataSource.currentUser != null
}
