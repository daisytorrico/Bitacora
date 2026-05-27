package com.catedra.bitacora.features.travel.presentation.pointCreate

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.catedra.bitacora.ui.components.AppDatePickerField
import com.catedra.bitacora.ui.components.AppTimePickerField
import com.catedra.bitacora.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePointScreen(
    onBack: () -> Unit,
    onPointCreated: (String) -> Unit,
    viewModel: CreatePointViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data?.clipData != null) {
                // Selección multiple
                val uris = mutableListOf<Uri>()
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    uris.add(data.clipData!!.getItemAt(i).uri)
                }
                viewModel.onImagesAdded(uris)
            } else {
                // Selección única o cámara
                val selectedUri = data?.data ?: viewModel.getActiveTempUri()
                selectedUri?.let { viewModel.onImageAdded(it) }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.obtenerUbicacionActual()
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.isSuccess, uiState.pointId) {
        val pointId = uiState.pointId
        if (uiState.isSuccess && pointId != null) {
            Toast.makeText(context, "Punto de interés guardado", Toast.LENGTH_SHORT).show()
            onPointCreated(pointId)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetError()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(titulo = "Añadir Punto", onBack = onBack)
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF4F6F8))
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Nombre
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Nombre del lugar") },
                    placeholder = { Text("Ej. Fontana di Trevi") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading
                )

                // Dirección / Ubicación
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.address,
                        onValueChange = { viewModel.onAddressChange(it) },
                        label = { Text("Dirección") },
                        placeholder = { Text("Calle o coordenadas") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                        },
                        enabled = !uiState.isLoading
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val fineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                val coarseLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                                
                                if (fineLoc == PackageManager.PERMISSION_GRANTED || coarseLoc == PackageManager.PERMISSION_GRANTED) {
                                    viewModel.obtenerUbicacionActual()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Usar ubicación actual", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { /* Ver en mapa estético */ },
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

                // Fecha de Visita
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val formatter = remember { java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy") }
                    val travel = uiState.travel
                    val rangeText = if (travel?.startDate != null && travel.endDate != null) {
                        "Rango del viaje: ${travel.startDate.format(formatter)} al ${travel.endDate.format(formatter)}"
                    } else ""

                    AppDatePickerField(
                        label = "Fecha de visita",
                        selectedDateMillis = uiState.visitDateMillis,
                        onDateSelected = { viewModel.onDateSelected(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AppTimePickerField(
                        label = "Hora de visita",
                        selectedHour = uiState.visitHour,
                        selectedMinute = uiState.visitMinute,
                        onTimeSelected = { hour, minute -> viewModel.onTimeSelected(hour, minute) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.isDateOutOfRange) {
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

                // Notas
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    label = { Text("Notas") },
                    placeholder = { Text("¿Qué lo hace especial?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    enabled = !uiState.isLoading
                )

                // Fotos del Lugar
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Fotos del lugar", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            AddPhotoButton(onClick = {
                                photoLauncher.launch(viewModel.buildPhotoPickerIntent())
                            })
                        }
                        items(uiState.selectedImages) { uri ->
                            PhotoItem(
                                uri = uri,
                                onRemove = { viewModel.onImageRemoved(uri) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Guardar
                Button(
                    onClick = { viewModel.savePoint() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    enabled = uiState.canSave
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.PinDrop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir al viaje", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            
            if (uiState.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.12f)
                ) {}
            }
        }
    }
}

@Composable
fun AddPhotoButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color.LightGray.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
            Text("Añadir", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PhotoItem(uri: Uri, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(80.dp)) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .padding(4.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.White, modifier = Modifier.size(12.dp))
        }
    }
}
