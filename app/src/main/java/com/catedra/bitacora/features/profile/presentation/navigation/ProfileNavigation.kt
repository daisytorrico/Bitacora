package com.catedra.bitacora.features.profile.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.catedra.bitacora.features.profile.presentation.edit.EditProfileScreen

object ProfileDestinations {
    const val EDIT_PROFILE = "edit_profile"
}

fun NavGraphBuilder.profileGraph(navController: NavController) {
    composable(ProfileDestinations.EDIT_PROFILE) {
        EditProfileScreen(
            onBack = { navController.popBackStack() }
        )
    }
}
