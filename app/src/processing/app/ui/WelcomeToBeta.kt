package processing.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography
import processing.app.Preferences
import processing.app.Base.getRevision
import processing.app.Base.getVersionName
import processing.app.ui.theme.LocalLocale
import processing.app.ui.theme.Locale
import processing.app.ui.theme.PDEComposeWindow
import processing.app.ui.theme.PDESwingWindow
import processing.app.ui.theme.ProcessingTheme
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
        @JvmStatic
        fun showWelcomeToBeta() {
            SwingUtilities.invokeLater {
                val close = {
                    Preferences.set("update.beta_welcome", getRevision().toString())
                }

                PDESwingWindow("beta.window.title", onClose = close) {
                    welcomeToBeta(close)
                }
            }
        }

        val windowSize = Dimension(400, 200)
        @Composable
        fun welcomeToBeta(close: () -> Unit = {}) {
            Row(
                modifier = Modifier
                    .padding(20.dp, 10.dp)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement
                    .spacedBy(20.dp)
            ){
                val locale = LocalLocale.current
                Image(
                    painter = painterResource("bird.svg"),
                    contentDescription = locale["beta.logo"],
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(120.dp)
                        .offset(0.dp, (-25).dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement
                        .spacedBy(
                            10.dp,
                            alignment = Alignment.CenterVertically
                        )
                ) {
                    Text(
                        text = locale["beta.title"],
                        style = typography.titleLarge,
                    )
                    val text = locale["beta.message"]
                        .replace('$' + "version", getVersionName())
                        .replace('$' + "revision", getRevision().toString())
                    Markdown(
                        text,
                        colors = markdownColor(),
                        typography = markdownTypography(),
                        modifier = Modifier.background(Color.Transparent).padding(bottom = 10.dp)
                    )
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = {
                            close()
                        }) {
                            Text(
                                text = locale["beta.button"],
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            application {
                PDEComposeWindow(titleKey = "beta.window.title", onClose = ::exitApplication){
                    welcomeToBeta {
                        exitApplication()
                    }
                }
            }
        }
    }
}

