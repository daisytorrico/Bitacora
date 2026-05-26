package com.catedra.bitacora.features.travel.presentation.travelCreate

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.catedra.bitacora.ui.components.AppDatePickerField
import com.catedra.bitacora.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTravelScreen(
    onBack: () -> Unit,
    viewModel: CreateTravelViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Manejo de eventos de navegación y errores
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetError()
        }
    }

    val selectorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedUri = result.data?.data ?: viewModel.getActiveTempUri()
            viewModel.onImageSelected(selectedUri)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.buildSystemChooserIntent().let { selectorLauncher.launch(it) }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(titulo = "Nuevo Viaje", onBack = onBack)
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF4F6F8))
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Selector de Imagen
                FormPortadaSelector(
                    imageUri = uiState.imageUri,
                    onClickAddFoto = {
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            viewModel.buildSystemChooserIntent().let { selectorLauncher.launch(it) }
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )

                // Nombre
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Nombre del viaje") },
                    placeholder = { Text("Ej. Verano en la Toscana") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.name.isBlank() && uiState.name.isNotEmpty(),
                    enabled = !uiState.loading
                )

                // Fechas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppDatePickerField(
                        label = "Inicio",
                        selectedDateMillis = uiState.startDate,
                        onDateSelected = { viewModel.onStartDateSelected(it) },
                        modifier = Modifier.weight(1f)
                    )

                    AppDatePickerField(
                        label = "Fin",
                        selectedDateMillis = uiState.endDate,
                        onDateSelected = { viewModel.onEndDateSelected(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (uiState.isDateInvalid) {
                    Text(
                        text = "La fecha de fin no puede ser anterior al inicio",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Descripción
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    label = { Text("Descripción") },
                    placeholder = { Text("¿Qué esperas de esta aventura?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 4,
                    enabled = !uiState.loading
                )

                Spacer(modifier = Modifier.weight(1f))

                // Botón Guardar
                Button(
                    onClick = { viewModel.saveTravel() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = uiState.canSave,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar Viaje", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // Overlay de carga
            if (uiState.loading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.1f)
                ) {}
            }
        }
    }
}
