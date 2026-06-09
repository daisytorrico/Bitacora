package com.catedra.bitacora.core.ui.components.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitacoraChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
    unselectedBorderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    selectedContainerAlpha: Float = 0.15f,
    selectedBorderAlpha: Float = 0.5f,
    selectedBorderWidth: Dp = 1.dp
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = unselectedColor,
            iconColor = unselectedColor,
            selectedContainerColor = activeColor.copy(alpha = selectedContainerAlpha),
            selectedLabelColor = activeColor,
            selectedLeadingIconColor = activeColor,
            selectedTrailingIconColor = activeColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = unselectedBorderColor,
            selectedBorderColor = activeColor.copy(alpha = selectedBorderAlpha),
            selectedBorderWidth = selectedBorderWidth
        )
    )
}
