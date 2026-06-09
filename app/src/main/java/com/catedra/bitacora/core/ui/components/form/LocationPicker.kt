package com.catedra.bitacora.core.ui.components.form

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.catedra.bitacora.R
import com.catedra.bitacora.core.ui.components.map.MapComponent
import com.catedra.bitacora.core.domain.model.PointOnMap

@Composable
fun LocationPicker(
    onLocationSelected: (PointOnMap) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
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
                buttonText = stringResource(R.string.confirm_ubication),
                onPointSelected = { point ->
                    onLocationSelected(point)
                }
            )
        }
    }
}
