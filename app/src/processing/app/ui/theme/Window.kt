package processing.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.formdev.flatlaf.util.SystemInfo
import java.awt.Dimension

import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JFrame
import javax.swing.UIManager

val LocalWindow = compositionLocalOf<JFrame> { error("No Window Set") }

/**
 * A utility class to create a new Window with Compose content in a Swing application.
 * It sets up the window with some default properties and allows for custom content.
 * Use this when creating a Compose based window from Swing.
 *
 * Usage example:
 * ```
 * SwingUtilities.invokeLater {
 *      PDESwingWindow("menu.help.welcome", fullWindowContent = true) {
 *
 *      }
 * }
 * ```
 *
 * @param titleKey The key for the window title, which will be localized.
 * @param size The desired size of the window. If null, the window will use its default size.
 * @param minSize The minimum size of the window. If null, no minimum size is set.
 * @param maxSize The maximum size of the window. If null, no maximum size is set.
 * @param fullWindowContent If true, the content will extend into the title bar area on macOS.
 * @param content The composable content to be displayed in the window.
 */
class PDESwingWindow(
    titleKey: String = "",
    size: Dimension? = null,
    minSize: Dimension? = null,
    maxSize: Dimension? = null,
    fullWindowContent: Boolean = false,
    onClose: () -> Unit = {},
    content: @Composable () -> Unit
){
    init{
        ComposeWindow().apply {
            val window = this
            defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            size?.let {
                window.size = it
            }
            minSize?.let {
                window.minimumSize = it
            }
            maxSize?.let {
                window.maximumSize = it
            }
            setLocationRelativeTo(null)
            setContent {
                PDEWindowContent(window, titleKey, fullWindowContent, content)
            }
            window.addWindowStateListener {
                if(it.newState == JFrame.DISPOSE_ON_CLOSE){
                    onClose()
                }
            }
            isVisible = true
        }
    }
}

/**
 * Internal Composable function to set up the window content with theming and localization.
 * It also handles macOS specific properties for full window content.
 *
 * @param window The JFrame instance to be configured.
 * @param titleKey The key for the window title, which will be localized.
 * @param fullWindowContent If true, the content will extend into the title bar area on macOS.
 * @param content The composable content to be displayed in the window.
 */
@Composable
private fun PDEWindowContent(
    window: ComposeWindow,
    titleKey: String,
    fullWindowContent: Boolean = false,
    content: @Composable () -> Unit
){
    val mac = SystemInfo.isMacOS && SystemInfo.isMacFullWindowContentSupported
    remember {
        window.rootPane.putClientProperty("apple.awt.fullWindowContent", mac && fullWindowContent)
        window.rootPane.putClientProperty("apple.awt.transparentTitleBar", mac && fullWindowContent)
    }

    CompositionLocalProvider(LocalWindow provides window) {
        PDETheme{
            val locale = LocalLocale.current
            window.title = locale[titleKey]
            content()
        }
    }
}

/**
 * A Composable function to create and display a new window with the specified content.
 * This function sets up the window state and handles the close request.
 * Use this when creating a Compose based window from another Compose context.
 *
 * Usage example:
 * ```
 * PDEComposeWindow("window.title", fullWindowContent = true, onClose = { /* handle close */ }) {
 *    // Your window content here
 *    Text("Hello, World!")
 * }
 * ```
 *
 * This will create a new window with the title localized from "window.title" key,
 * with content extending into the title bar area on macOS, and a custom close handler.
 *
 * Fully standalone example:
 * ```
 * application {
 *    PDEComposeWindow("window.title", fullWindowContent = true, onClose = ::exitApplication) {
 *      // Your window content here
 *    }
 * }
 * ```
 *
 * @param titleKey The key for the window title, which will be localized.
 * @param size The desired size of the window. Defaults to unspecified size which means the window will be
 * fullscreen if it contains any of [fillMaxWidth]/[fillMaxSize]/[fillMaxHeight] etc.
 * @param minSize The minimum size of the window. Defaults to unspecified size which means no minimum size is set.
 * @param maxSize The maximum size of the window. Defaults to unspecified size which means no maximum size is set.
 * @param fullWindowContent If true, the content will extend into the title bar area on
 * macOS.
 * @param onClose A lambda function to be called when the window is requested to close.
 * @param content The composable content to be displayed in the window.
 *
 *
 *
 */
@Composable
fun PDEComposeWindow(
    titleKey: String,
    size: DpSize = DpSize.Unspecified,
    minSize: DpSize = DpSize.Unspecified,
    maxSize: DpSize = DpSize.Unspecified,
    fullWindowContent: Boolean = false,
    onClose: () -> Unit = {},
    content: @Composable () -> Unit
){
    val windowState = rememberWindowState(
        size = size,
        position = WindowPosition(Alignment.Center)
    )
    Window(onCloseRequest = onClose, state = windowState, title = "") {
        remember {
            window.minimumSize = minSize.toDimension()
            window.maximumSize = maxSize.toDimension()
        }
        PDEWindowContent(window, titleKey, fullWindowContent, content)
    }
}

fun DpSize.toDimension(): Dimension? {
    if(this == DpSize.Unspecified) { return null }

    return Dimension(
        this.width.value.toInt(),
        this.height.value.toInt()
    )
}

fun main(){
    application {
        PDEComposeWindow(
            onClose = ::exitApplication,
            titleKey = "window.title",
            size = DpSize(800.dp, 600.dp),
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("Hello, World!")
            }
        }
    }
}