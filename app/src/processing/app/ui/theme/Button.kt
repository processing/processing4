package processing.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import java.awt.Cursor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PDEButton(onClick: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    val theme = LocalTheme.current

    var hover by remember { mutableStateOf(false) }
    var clicked by remember { mutableStateOf(false) }
    val offset by animateFloatAsState(if (hover) -5f else 5f)
    val color by animateColorAsState(if(clicked) colors.primaryVariant else colors.primary)

    Box(modifier = Modifier.padding(end = 5.dp, top = 5.dp)) {
        Box(
            modifier = Modifier
                .offset((-offset).dp, (offset).dp)
                .background(theme.getColor("toolbar.button.pressed.field"))
                .matchParentSize()
        )
        Box(
            modifier = Modifier
                .onPointerEvent(PointerEventType.Press) {
                    clicked = true
                }
                .onPointerEvent(PointerEventType.Release) {
                    clicked = false
                    onClick()
                }
                .onPointerEvent(PointerEventType.Enter) {
                    hover = true
                }
                .onPointerEvent(PointerEventType.Exit) {
                    hover = false
                }
                .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                .background(color)
                .padding(10.dp)
                .sizeIn(minWidth = 100.dp),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}