@file:JvmName("ComposeTopBarBridge")
package processing.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import processing.app.Base
import processing.app.Preferences
import processing.app.UpdateCheck
import java.awt.Color as AwtColor
import javax.swing.JMenuItem
import javax.swing.JPopupMenu

fun awtToCompose(c: AwtColor): Color {
    return Color(c.red, c.green, c.blue, c.alpha)
}

fun themeColorOrFallback(key: String, fallback: AwtColor): Color {
    val awt = Theme.getColor(key) ?: fallback
    return awtToCompose(awt)
}

@Composable
fun TopBar(panel: ComposePanel, base: Base) {
    val blueBarColor = themeColorOrFallback("toolbar.gradient.top", AwtColor(107, 160, 204))
    val textColor = themeColorOrFallback("toolbar.rollover.color", AwtColor(0, 0, 0))

    val developAnchor = remember { mutableStateOf(IntOffset.Zero) }
    val developHeight = remember { mutableStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(blueBarColor)
            .padding(start = 8.dp)
    ) {
        Box(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                developAnchor.value = IntOffset(
                    position.x.toInt(),
                    position.y.toInt()
                )
                developHeight.value = coordinates.size.height
            }

        ) {
            Text(
                text = "Develop",
                color = textColor,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable {
                        showDevelopPopup(
                            panel = panel,
                            base = base,
                            x = developAnchor.value.x,
                            y = developAnchor.value.y + developHeight.value
                        )
                    }
            )
            Text (
                text = "New Button",
                color = textColor,
                modifier = Modifier

                    .padding(horizontal = 40.dp, vertical = 4.dp)
                    .clickable {
                        showDevelopPopup(
                            panel = panel,
                            base = base,
                            x = developAnchor.value.x,
                            y = developAnchor.value.y + developHeight.value
                        )
                    }
            )
        }
    }
}

private fun showDevelopPopup(panel: ComposePanel, base: Base, x: Int, y: Int) {
    val popup = JPopupMenu()

    val updatesItem = JMenuItem("Check for Updates")
    updatesItem.addActionListener {
        Preferences.unset("update.last")
        Preferences.setInteger("update.beta_welcome", 0)
        UpdateCheck(base)
    }

    popup.add(updatesItem)
    popup.show(panel, x, y) //this is ignoring the os differences
}

fun mountTopBar(panel: ComposePanel, base: Base) {
    val awtBg = Theme.getColor("toolbar.gradient.top") ?: AwtColor(107, 160, 204)
    panel.background = awtBg

    panel.setContent {
        TopBar(panel, base)
    }
}