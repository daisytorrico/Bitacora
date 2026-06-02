package com.catedra.bitacora.core.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.catedra.bitacora.core.ui.theme.GrisMedio
import com.catedra.bitacora.core.ui.theme.GrisSeparador

@Composable
fun ProfileImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(GrisSeparador),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank() && !isError) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profile Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { isError = true }
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                contentDescription = "Viajerito",
                modifier = Modifier.size((size * 0.6).dp),
                tint = GrisMedio
            )
        }
    }
}
