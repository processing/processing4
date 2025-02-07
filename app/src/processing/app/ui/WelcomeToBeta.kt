package processing.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.formdev.flatlaf.util.SystemInfo
import processing.app.ui.theme.*
import java.awt.Cursor
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.InputStream
import java.util.Properties
import javax.swing.JFrame
import javax.swing.SwingUtilities


class WelcomeToBeta {
    companion object{
        val windowSize = Dimension(400, 200)

        @JvmStatic
        fun showWelcomeToBeta() {
            SwingUtilities.invokeLater {
                PDEWindow("beta.window.title") {
                    welcomeToBeta()
                }
            }
        }

        @Composable
        fun welcomeToBeta(close: () -> Unit = {}) {
            Row(
                modifier = Modifier
                    .padding(20.dp, 10.dp)
                    .size(windowSize.width.dp, windowSize.height.dp),
                horizontalArrangement = Arrangement
                    .spacedBy(20.dp)
            ){
                val locale = LocalLocale.current
                Image(
                    painter = painterResource("logo.svg"),
                    contentDescription = locale["beta.logo"],
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(100.dp, 100.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement
                        .spacedBy(
                            MaterialTheme.typography.subtitle1.lineHeight.value.dp,
                            alignment = Alignment.CenterVertically
                        )
                ) {
                    Text(
                        text = locale["beta.title"],
                        style = MaterialTheme.typography.subtitle1,
                    )
                    Text(
                        text = locale["beta.message"]
                    )
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        PDEButton(onClick = {
                            close()
                        }) {
                            Text(
                                text = locale["beta.button"],
                                color = colors.onPrimary
                            )
                        }
                    }
                }
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            pdeapplication("beta.window.title") {
                welcomeToBeta()
            }
        }
    }
}

