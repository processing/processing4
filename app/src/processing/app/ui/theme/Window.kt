package processing.app.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.formdev.flatlaf.util.SystemInfo

import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JFrame

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
 * @param fullWindowContent If true, the content will extend into the title bar area on macOS.
 * @param content The composable content to be displayed in the window.
 */
class PDESwingWindow(titleKey: String = "", fullWindowContent: Boolean = false, onClose: () -> Unit = {}, content: @Composable BoxScope.() -> Unit): JFrame(){
    init{
        val window = this
        defaultCloseOperation = DISPOSE_ON_CLOSE
        ComposePanel().apply {
            setContent {
                PDEWindowContent(window, titleKey, fullWindowContent, content)
            }
            window.add(this)
        }
        background = java.awt.Color.white
        setLocationRelativeTo(null)
        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode != KeyEvent.VK_ESCAPE) return

                window.dispose()
                onClose()
            }
        })
        isResizable = false
        isVisible = true
        requestFocus()
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
private fun PDEWindowContent(window: JFrame, titleKey: String, fullWindowContent: Boolean = false, content: @Composable BoxScope.() -> Unit){
    val mac = SystemInfo.isMacOS && SystemInfo.isMacFullWindowContentSupported
    remember {
        window.rootPane.putClientProperty("apple.awt.fullWindowContent", mac && fullWindowContent)
        window.rootPane.putClientProperty("apple.awt.transparentTitleBar", mac && fullWindowContent)
    }

    CompositionLocalProvider(LocalWindow provides window) {
        PDETheme {
            val locale = LocalLocale.current
            window.title = locale[titleKey]
            LaunchedEffect(locale) {
                window.pack()
                window.setLocationRelativeTo(null)
            }

            Box(modifier = Modifier.padding(top = if (mac && !fullWindowContent) 22.dp else 0.dp),content = content)
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
 * @param fullWindowContent If true, the content will extend into the title bar area on
 * macOS.
 * @param onClose A lambda function to be called when the window is requested to close.
 * @param content The composable content to be displayed in the window.
 *
 *
 *
 */
@Composable
fun PDEComposeWindow(titleKey: String, fullWindowContent: Boolean = false, onClose: () -> Unit = {}, content: @Composable BoxScope.() -> Unit){
    val windowState = rememberWindowState(
        size = DpSize.Unspecified,
        position = WindowPosition(Alignment.Center)
    )
    Window(onCloseRequest = onClose, state = windowState, title = "") {
        PDEWindowContent(window, titleKey, fullWindowContent, content)
    }
}