package processing.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        border = BorderStroke(1.dp, colors.secondary),
        colors = ChipDefaults.chipColors(
            backgroundColor = colors.background,
            contentColor = colors.primaryVariant
        ),
        leadingIcon = leadingIcon,
        modifier = Modifier,
        content = content
    )
}