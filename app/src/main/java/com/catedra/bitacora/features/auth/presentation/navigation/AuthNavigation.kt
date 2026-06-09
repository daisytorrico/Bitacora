package com.catedra.bitacora.features.auth.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.catedra.bitacora.features.auth.presentation.AuthViewModel
import com.catedra.bitacora.features.auth.presentation.login.LoginScreen
import com.catedra.bitacora.features.auth.presentation.register.RegisterScreen
import com.catedra.bitacora.features.auth.presentation.username.UsernameScreen

object AuthDestinations {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val USERNAME = "username"
}

fun NavGraphBuilder.authGraph(
    navController: NavController,
    viewModel: AuthViewModel,
    onGoogleSignInClick: () -> Unit
) {
    composable(AuthDestinations.LOGIN) {
        val authState by viewModel.authState.collectAsStateWithLifecycle()
        val email by viewModel.loginEmail.collectAsStateWithLifecycle()
        val password by viewModel.loginPassword.collectAsStateWithLifecycle()

        LoginScreen(
            authState = authState,
            email = email,
            onEmailChange = viewModel::onLoginEmailChange,
            password = password,
            onPasswordChange = viewModel::onLoginPasswordChange,
            onLoginClick = viewModel::login,
            onGoogleSignInClick = onGoogleSignInClick,
            onNavigateToRegister = { 
                viewModel.clearError()
                navController.navigate(AuthDestinations.REGISTRO) 
            },
            onResetError = viewModel::clearError
        )
    }

    composable(AuthDestinations.REGISTRO) {
        val authState by viewModel.authState.collectAsStateWithLifecycle()
        val name by viewModel.registerName.collectAsStateWithLifecycle()
        val email by viewModel.registerEmail.collectAsStateWithLifecycle()
        val password by viewModel.registerPassword.collectAsStateWithLifecycle()
        val confirmPassword by viewModel.registerConfirmPassword.collectAsStateWithLifecycle()

        RegisterScreen(
            authState = authState,
            name = name,
            onNameChange = viewModel::onRegisterNameChange,
            email = email,
            onEmailChange = viewModel::onRegisterEmailChange,
            password = password,
            onPasswordChange = viewModel::onRegisterPasswordChange,
            confirmPassword = confirmPassword,
            onConfirmPasswordChange = viewModel::onRegisterConfirmPasswordChange,
            onRegisterClick = viewModel::register,
            onNavigateToLogin = { 
                viewModel.clearError()
                navController.popBackStack() 
            },
            onResetError = viewModel::clearError
        )
    }

    composable(AuthDestinations.USERNAME) {
        val authState by viewModel.authState.collectAsStateWithLifecycle()
        val username by viewModel.username.collectAsStateWithLifecycle()
        val usernameError by viewModel.usernameError.collectAsStateWithLifecycle()

        UsernameScreen(
            authState = authState,
            username = username,
            onUsernameChange = viewModel::onUsernameChange,
            usernameError = usernameError,
            onConfirmClick = viewModel::saveUsername,
            onResetError = viewModel::clearUsernameError,
            onLogout = viewModel::logout
        )
    }
}
