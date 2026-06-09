package com.catedra.bitacora.features.travel.presentation.pointEdit

import android.net.Uri
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.catedra.bitacora.R
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.ui.components.common.AppTopBar
import com.catedra.bitacora.core.ui.components.form.LocationPicker
import com.catedra.bitacora.core.ui.components.form.PointFormContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPointScreen(
    onBack: () -> Unit,
    onPointUpdated: () -> Unit,
    viewModel: EditPointViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, context.getString(R.string.updated_point), Toast.LENGTH_SHORT).show()
            onPointUpdated()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetError()
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data?.clipData != null) {
                val uris = mutableListOf<android.net.Uri>()
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
            Toast.makeText(context, context.getString(R.string.denied_cammera), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = stringResource(R.string.edit_point),
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { if (uiState.canSave) viewModel.setShowConfirmDialog(true) },
                        enabled = uiState.canSave && !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isInitialLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)) {
                    PointFormContent(
                        name = uiState.name,
                        onNameChange = { viewModel.onNameChange(it) },
                        address = uiState.address,
                        onAddressChange = { viewModel.onAddressChange(it) },
                        onMyLocationClick = { /* Implementar */ },
                        onMapPickerClick = { viewModel.onToggleMap(true) },
                        visitDateMillis = uiState.visitDateMillis,
                        onDateSelected = { viewModel.onDateSelected(it) },
                        visitHour = uiState.visitHour,
                        visitMinute = uiState.visitMinute,
                        onTimeSelected = { h, m -> viewModel.onTimeSelected(h, m) },
                        notes = uiState.notes,
                        onNotesChange = { viewModel.onNotesChange(it) },
                        selectedImages = uiState.selectedImages,
                        remoteImageUrls = uiState.remoteImageUrls,
                        onAddPhotoClick = {
                            val cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
                                photoLauncher.launch(viewModel.buildPhotoPickerIntent())
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        onRemovePhoto = { viewModel.onImageRemoved(it) },
                        onRemoveRemotePhoto = { viewModel.onRemoteImageRemoved(it) },
                        isLoading = uiState.isLoading,
                        travel = uiState.travel,
                        isDateOutOfRange = uiState.isDateOutOfRange
                    )

                    if (uiState.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                if (uiState.showConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { viewModel.setShowConfirmDialog(false) },
                        title = { Text(stringResource(R.string.confirm_changes)) },
                        text = { Text(stringResource(R.string.confirm_poi_changes)) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.updatePoint() }) {
                                Text(stringResource(R.string.save))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.setShowConfirmDialog(false) }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }

                if (uiState.showMapSelector) {
                    LocationPicker(
                        onLocationSelected = { point ->
                            viewModel.onAddressChange(point.address)
                            viewModel.onToggleMap(false)
                        },
                        onDismiss = { viewModel.onToggleMap(false) }
                    )
                }
            }
        }
    }
}
