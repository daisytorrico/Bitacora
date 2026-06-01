package com.catedra.bitacora.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.catedra.bitacora.core.components.map.MapComponent
import com.catedra.bitacora.core.domain.model.PointOnMap

@Composable
fun LocationPicker(
    onLocationSelected: (PointOnMap) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Para que ocupe toda la pantalla
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            MapComponent(
                modifier = Modifier.fillMaxSize(),
                buttonText = "Confirmar ubicación",
                onPointSelected = { point ->
                    onLocationSelected(point)
                }
            )
        }
    }
}
