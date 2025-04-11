package processing.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import java.awt.Cursor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PDEButton(onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    var hover by remember { mutableStateOf(false) }
    val offset by animateFloatAsState(if (hover) -3f else 3f)

    Box {
        Box(
            modifier = Modifier
                .offset((-offset).dp, (offset).dp)
                .matchParentSize()
                .padding(vertical = 6.dp)
                .background(colors.secondary)

        )
        Button(
            onClick = onClick,
            shape = RectangleShape,
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 32.dp),
            modifier = Modifier
                .onPointerEvent(PointerEventType.Enter) {
                    hover = true
                }
                .onPointerEvent(PointerEventType.Exit) {
                    hover = false
                }
                .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
            content = content
        )
    }
}