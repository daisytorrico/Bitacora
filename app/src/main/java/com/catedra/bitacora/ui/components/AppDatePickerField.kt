package com.catedra.bitacora.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerField(
    label: String,
    selectedDateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateText = remember(selectedDateMillis) {
        if (selectedDateMillis != null) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC") // Forzamos UTC para evitar el salto de día
            sdf.format(Date(selectedDateMillis))
        } else ""
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = dateText,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
            },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        onDateSelected(datePickerState.selectedDateMillis)
                        showDatePicker = false
                    }) { Text("Aceptar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
