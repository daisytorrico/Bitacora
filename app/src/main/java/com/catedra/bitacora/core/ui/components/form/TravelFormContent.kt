package com.catedra.bitacora.core.ui.components.form

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catedra.bitacora.features.travel.domain.model.TravelVisibility
import com.catedra.bitacora.features.travel.presentation.travelCreate.FormPortadaSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelFormContent(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    startDate: Long?,
    onStartDateSelected: (Long?) -> Unit,
    endDate: Long?,
    onEndDateSelected: (Long?) -> Unit,
    visibility: TravelVisibility,
    onVisibilityChange: (TravelVisibility) -> Unit,
    imageUri: Uri?,
    imageUrl: String?,
    onClickAddFoto: () -> Unit,
    isDateInvalid: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        FormPortadaSelector(
            imageUri = imageUri ?: imageUrl?.let { Uri.parse(it) },
            onClickAddFoto = onClickAddFoto
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre del viaje") },
            placeholder = { Text("Ej. Verano en la Toscana") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = name.isBlank() && name.isNotEmpty(),
            enabled = !isLoading
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppDatePickerField(
                label = "Inicio",
                selectedDateMillis = startDate,
                onDateSelected = onStartDateSelected,
                modifier = Modifier.weight(1f)
            )

            AppDatePickerField(
                label = "Fin",
                selectedDateMillis = endDate,
                onDateSelected = onEndDateSelected,
                modifier = Modifier.weight(1f)
            )
        }
        
        if (isDateInvalid) {
            Text(
                text = "La fecha de fin no puede ser anterior al inicio",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Descripción") },
            placeholder = { Text("¿Qué esperas de esta aventura?") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 4,
            enabled = !isLoading
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Visibilidad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TravelVisibility.entries.forEachIndexed { index, vis ->
                    SegmentedButton(
                        selected = visibility == vis,
                        onClick = { onVisibilityChange(vis) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = TravelVisibility.entries.size
                        ),
                        label = {
                            Text(
                                when (vis) {
                                    TravelVisibility.PUBLIC -> "Público"
                                    TravelVisibility.PRIVATE -> "Privado"
                                    TravelVisibility.FOLLOWERS -> "Seguidores"
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}
