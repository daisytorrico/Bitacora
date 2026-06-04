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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.catedra.bitacora.core.utils.LocationPermissionHandler
import com.catedra.bitacora.core.utils.LocationPermissionUtils
import com.catedra.bitacora.core.ui.components.common.AppTopBar
import com.catedra.bitacora.core.ui.components.form.LocationPicker
import com.catedra.bitacora.core.ui.components.form.PointFormContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePointScreen(
    onBack: () -> Unit,
    onPointCreated: (String) -> Unit,
    viewModel: CreatePointViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showLocationPermissionHandler by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data?.clipData != null) {
                val uris = mutableListOf<Uri>()
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    uris.add(data.clipData!!.getItemAt(i).uri)
                }
                viewModel.onImagesAdded(uris)
            } else {
                val selectedUri = data?.data ?: viewModel.getActiveTempUri()
                selectedUri?.let { viewModel.onImageAdded(it) }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            photoLauncher.launch(viewModel.buildPhotoPickerIntent())
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    if (showLocationPermissionHandler) {
        LocationPermissionHandler { granted ->
            showLocationPermissionHandler = false
            if (granted) {
                viewModel.obtenerUbicacionActual()
            }
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
            AppTopBar(
                titulo = "Añadir Punto",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.savePoint() },
                        enabled = uiState.canSave && !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                PointFormContent(
                    name = uiState.name,
                    onNameChange = { viewModel.onNameChange(it) },
                    address = uiState.address,
                    onAddressChange = { viewModel.onAddressChange(it) },
                    onMyLocationClick = {
                        if (LocationPermissionUtils.hasLocationPermission(context)) {
                            viewModel.obtenerUbicacionActual()
                        } else {
                            showLocationPermissionHandler = true
                        }
                    },
                    onMapPickerClick = { viewModel.onToggleMap(true) },
                    visitDateMillis = uiState.visitDateMillis,
                    onDateSelected = { viewModel.onDateSelected(it) },
                    visitHour = uiState.visitHour,
                    visitMinute = uiState.visitMinute,
                    onTimeSelected = { h, m -> viewModel.onTimeSelected(h, m) },
                    notes = uiState.notes,
                    onNotesChange = { viewModel.onNotesChange(it) },
                    selectedImages = uiState.selectedImages,
                    remoteImageUrls = emptyList(),
                    onAddPhotoClick = {
                        val cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
                            photoLauncher.launch(viewModel.buildPhotoPickerIntent())
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onRemovePhoto = { viewModel.onImageRemoved(it) },
                    onRemoveRemotePhoto = {},
                    isLoading = uiState.isLoading,
                    travel = uiState.travel,
                    isDateOutOfRange = uiState.isDateOutOfRange,
                    modifier = Modifier.weight(1f)
                )

                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            
            if (uiState.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.12f)
                ) {}
            }

            if (uiState.showMapSelector) {
                LocationPicker(
                    onLocationSelected = { point ->
                        viewModel.onLocationSelected(
                            address = point.address,
                            latitude = point.coordinates.latitude,
                            longitude = point.coordinates.longitude
                        )
                    },
                    onDismiss = { viewModel.onToggleMap(false) }
                )
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
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
