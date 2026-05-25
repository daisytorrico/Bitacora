package com.catedra.bitacora.features.travel.presentation.travelCreate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.catedra.bitacora.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTravelScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Nuevo Viaje",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Aquí irá el formulario para crear un viaje")
        }
    }
}
