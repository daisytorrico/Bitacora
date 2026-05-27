package com.catedra.bitacora.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerField(
    label: String,
    selectedHour: Int?,
    selectedMinute: Int?,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val timeText = remember(selectedHour, selectedMinute) {
        if (selectedHour != null && selectedMinute != null) {
            String.format("%02d:%02d", selectedHour, selectedMinute)
        } else ""
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = timeText,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                Icon(Icons.Default.Schedule, contentDescription = null)
            },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = selectedHour ?: 12,
                initialMinute = selectedMinute ?: 0,
                is24Hour = true
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }) { Text("Aceptar") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
                },
                text = { TimePicker(state = timePickerState) }
            )
        }
    }
}