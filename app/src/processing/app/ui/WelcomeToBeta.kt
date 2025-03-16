package processing.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
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
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m2.markdownColor
import com.mikepenz.markdown.m2.markdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography
import processing.app.Base.getRevision
import processing.app.Base.getVersionName
import processing.app.ui.theme.LocalLocale
import processing.app.ui.theme.LocalTheme
import processing.app.ui.theme.Locale
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
        fun welcomeToBeta() {
            Row(
                modifier = Modifier
                    .padding(20.dp, 10.dp)
                    .size(windowSize.width.dp, windowSize.height.dp),
                horizontalArrangement = Arrangement
                    .spacedBy(20.dp)
            ){
                val locale = LocalLocale.current
                Image(
                    painter = painterResource("bird.svg"),
                    contentDescription = locale["beta.logo"],
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(100.dp, 100.dp)
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
                        style = typography.subtitle1,
                    )
                    val text = locale["beta.message"]
                        .replace('$' + "version", getVersionName())
                        .replace('$' + "revision", getRevision().toString())
                    Markdown(
                        text,
                        colors = markdownColor(),
                        typography = markdownTypography(text = typography.body1, link = typography.body1.copy(color = colors.primary)),
                        modifier = Modifier.background(Color.Transparent).padding(bottom = 10.dp)
                    )
                    Row {
                        val window = LocalWindow.current
                        Spacer(modifier = Modifier.weight(1f))
                        PDEButton(onClick = {
                            window.dispose()
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

