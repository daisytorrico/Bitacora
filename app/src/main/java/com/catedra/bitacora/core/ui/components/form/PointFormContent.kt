package com.catedra.bitacora.core.ui.components.form

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.presentation.pointCreate.AddPhotoButton
import com.catedra.bitacora.features.travel.presentation.pointCreate.PhotoItem

@Composable
fun PointFormContent(
    name: String,
    onNameChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    onMyLocationClick: () -> Unit,
    onMapPickerClick: () -> Unit,
    visitDateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    visitHour: Int?,
    visitMinute: Int?,
    onTimeSelected: (Int, Int) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    selectedImages: List<Uri>,
    remoteImageUrls: List<String>,
    onAddPhotoClick: () -> Unit,
    onRemovePhoto: (Uri) -> Unit,
    onRemoveRemotePhoto: (String) -> Unit,
    isLoading: Boolean,
    travel: Travel? = null,
    isDateOutOfRange: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre del lugar") },
            placeholder = { Text("Ej. Fontana di Trevi") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text("Dirección") },
                placeholder = { Text("Calle o coordenadas") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                },
                enabled = !isLoading
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onMyLocationClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mi ubicación", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onMapPickerClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver en mapa", fontSize = 12.sp)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val formatter = remember { java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy") }
            val rangeText = if (travel?.startDate != null && travel.endDate != null) {
                "Rango del viaje: ${travel.startDate.format(formatter)} al ${travel.endDate.format(formatter)}"
            } else ""

            AppDatePickerField(
                label = "Fecha de visita",
                selectedDateMillis = visitDateMillis,
                onDateSelected = onDateSelected,
                modifier = Modifier.fillMaxWidth()
            )
            AppTimePickerField(
                label = "Hora de visita",
                selectedHour = visitHour,
                selectedMinute = visitMinute,
                onTimeSelected = onTimeSelected,
                modifier = Modifier.fillMaxWidth()
            )

            if (isDateOutOfRange) {
                Text(
                    text = "Fuera de rango. $rangeText",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 12.dp)
                )
            } else if (rangeText.isNotEmpty()) {
                Text(
                    text = rangeText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notas") },
            placeholder = { Text("¿Qué lo hace especial?") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            enabled = !isLoading
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Fotos del lugar", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    AddPhotoButton(onClick = onAddPhotoClick)
                }
                items(remoteImageUrls) { url ->
                    PhotoItem(
                        uri = Uri.parse(url),
                        onRemove = { onRemoveRemotePhoto(url) }
                    )
                }
                items(selectedImages) { uri ->
                    PhotoItem(
                        uri = uri,
                        onRemove = { onRemovePhoto(uri) }
                    )
                }
            }
        }
    }
}
