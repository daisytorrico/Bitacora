package com.catedra.bitacora.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.catedra.bitacora.features.auth.domain.model.User
import com.catedra.bitacora.ui.theme.GrisMedio

//Componente de cabecera de perfil reutilizable.

@Composable
fun ProfileHeader(
    user: User?,
    travelCount: Int,
    modifier: Modifier = Modifier,
    onEditClick: (() -> Unit)? = null, // Si es null, el botón de edición desaparece
    actions: @Composable (RowScope.() -> Unit)? = null // Slot para botones extra (ej: Follow)
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Foto
            AsyncImage(
                model = user?.photoUrl ?: "https://via.placeholder.com/150",
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.displayName ?: "Cargando...",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = "@${user?.username ?: "usuario"}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = GrisMedio
                    )
                )
                if (!user?.bio.isNullOrBlank()) {
                    Text(
                        text = user?.bio!!,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Acciones dinámicas
            if (onEditClick != null) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Perfil",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            actions?.invoke(this)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Contador simplificado
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.TravelExplore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$travelCount",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
