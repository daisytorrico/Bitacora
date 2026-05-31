package com.catedra.bitacora.features.map.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.catedra.bitacora.core.components.map.MapComponent
import com.catedra.bitacora.ui.components.AppBottomBar
import com.catedra.bitacora.ui.components.AppTopBar

@Composable
fun MapScreen(navController: NavController, onLogout: () -> Unit) {
    Scaffold(topBar = {
        AppTopBar(
            titulo = "Mapa cerca de tí", actions = {
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Cerrar Sesión"
                    )
                }
            })
    }, bottomBar = {
        AppBottomBar(navController = navController)
    }) { paddingValues ->
        MapComponent(
            modifier = Modifier.padding(paddingValues),
            externalPois = emptyList(),
            onExternalPoiSelected = { point ->
                // Todo: Navegar al detalle si es necesario
            },
            externalPoiButtonText = "Detalles"
        )
    }
}
