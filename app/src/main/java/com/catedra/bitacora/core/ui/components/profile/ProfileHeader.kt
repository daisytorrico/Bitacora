package com.catedra.bitacora.core.ui.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catedra.bitacora.R
import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.core.ui.theme.GrisMedio

@Composable
fun ProfileHeader(
    user: User?,
    travelCount: Int,
    modifier: Modifier = Modifier,
    onEditClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileImage(imageUrl = user?.photoUrl, size = 72)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.displayName ?: stringResource(R.string.loading),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = "@${user?.username ?: stringResource(R.string.user)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = GrisMedio)
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

            if (onEditClick != null) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_profile),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            actions?.invoke(this)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Fila de Contadores
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CounterItem(Icons.Default.TravelExplore, travelCount, stringResource(R.string.travels))
            CounterItem(Icons.Default.People, user?.followersCount ?: 0,
                stringResource(R.string.followers)
            )
            CounterItem(Icons.Default.PersonAdd, user?.followingCount ?: 0,
                stringResource(R.string.followed)
            )
        }
    }
}

@Composable
private fun CounterItem(icon: ImageVector, count: Int, label: String) {
    val color = MaterialTheme.colorScheme.primary
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}
