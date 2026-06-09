package com.catedra.bitacora.core.ui.components.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catedra.bitacora.R
import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.core.ui.theme.GrisMedio

@Composable
fun UserHeader(
    user: User?,
    modifier: Modifier = Modifier,
    avatarSize: Int = 40,
    showBadges: Boolean = false,
    onClick: (() -> Unit)? = null,
    badges: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileImage(
            imageUrl = user?.photoUrl,
            size = avatarSize
        )
        
        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "@${user?.username ?: stringResource(R.string.user)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GrisMedio
                )
            )
            if (showBadges && badges != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    badges()
                }
            }
        }
    }
}
