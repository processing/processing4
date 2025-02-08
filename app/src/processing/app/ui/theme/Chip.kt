package processing.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Chip
import androidx.compose.material.ChipColors
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PDEChip(
    onClick: () -> Unit = {},
    leadingIcon: @Composable (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
){
    Chip(
        onClick = onClick,
        border = BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.12f)),
        colors = ChipDefaults.chipColors(
            backgroundColor = Color.Transparent,
            contentColor = colors.onSurface
        ),
        leadingIcon = leadingIcon,
        modifier = Modifier,
        content = content
    )
}