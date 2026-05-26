package com.catedra.bitacora.features.map_test.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.catedra.bitacora.core.components.map.MapComponent
import com.catedra.bitacora.core.components.map.MapViewModel
import com.catedra.bitacora.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTestScreen(
    onBack: () -> Unit
) {
    val viewModel: MapViewModel = hiltViewModel()
    var selectedInfo by remember { mutableStateOf("Ningún punto seleccionado") }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Prueba de Mapa",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text(
                text = selectedInfo,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            
            MapComponent(
                viewModel = viewModel,
                onPointSelected = { point ->
                    selectedInfo = "Seleccionado: ${point.name} (${point.coordinates.latitude}, ${point.coordinates.longitude})"
                },
                buttonText = "Confirmar Punto",
                modifier = Modifier.weight(1f)
            )
        }
    }
}
