package com.catedra.bitacora.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.catedra.bitacora.ui.auth.AuthState
import com.catedra.bitacora.ui.auth.AuthViewModel
import com.catedra.bitacora.ui.auth.LoginScreen
import com.catedra.bitacora.ui.auth.RegisterScreen
import com.catedra.bitacora.ui.home.HomeScreen
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val HOME = "home"
}

@Composable
fun AppNavigation(viewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Credential Manager setup
    val credentialManager = CredentialManager.create(context)

    val onGoogleSignInClick: () -> Unit = {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("1901113908-r6sliik0sosrd0a7p9n7v1o11bih36pm.apps.googleusercontent.com")
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                Log.d("Auth", "Iniciando petición de Credential Manager...")
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val credential = result.credential

                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.iniciarSesionConGoogle(googleIdTokenCredential.idToken)
                } else {
                    Log.w("Auth", "Tipo de credencial no esperado: ${credential.type}")
                }
            } catch (e: GetCredentialException) {
                Log.e("Auth", "Error al obtener credenciales (Credential Manager): ${e.message}")
                viewModel.resetearError()
            } catch (e: Exception) {
                Log.e("Auth", "Error inesperado en flujo de Google: ${e.message}", e)
            }
        }
    }

    // Efecto para reaccionar a cambios de autenticación
    LaunchedEffect(authState) {
        Log.d("NAV", "Estado: $authState | Ruta: ${navController.currentDestination?.route}")
        when (authState) {
            is AuthState.Cargando -> {}
            is AuthState.Autenticado -> {
                navController.navigate(Rutas.HOME) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is AuthState.NoAutenticado -> {
                if (navController.currentDestination?.route != Rutas.LOGIN &&
                    navController.currentDestination?.route != Rutas.REGISTRO) {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Error -> {
                Log.e("AppNavigation", "Error: ${(authState as AuthState.Error).mensaje}")
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authState is AuthState.Autenticado) Rutas.HOME else Rutas.LOGIN
    ) {
        composable(Rutas.LOGIN) {
            if (authState !is AuthState.Cargando) {
                LoginScreen(
                    authState = authState,
                    onLoginClick = { email, pass -> viewModel.iniciarSesion(email, pass) },
                    onGoogleSignInClick = onGoogleSignInClick,
                    onNavigateToRegister = { navController.navigate(Rutas.REGISTRO) }
                )
            }
        }

        composable(Rutas.REGISTRO) {
            RegisterScreen(
                authState = authState,
                onRegisterClick = { email, pass -> viewModel.registrar(email, pass) },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Rutas.HOME) {
            HomeScreen(
                onCerrarSesion = { viewModel.cerrarSesion() }
            )
        }
    }
}
