package processing.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.formdev.flatlaf.util.SystemInfo
import processing.app.ui.WelcomeToBeta.Companion.welcomeToBeta

import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JFrame


class PDEWindow(titleKey: String = "", fullWindowContent: Boolean = false, content: @Composable () -> Unit): JFrame(){
    init{
        val mac = SystemInfo.isMacFullWindowContentSupported

        rootPane.apply{
            putClientProperty("apple.awt.transparentTitleBar", mac)
            putClientProperty("apple.awt.fullWindowContent", mac)
        }

        defaultCloseOperation = DISPOSE_ON_CLOSE
        ComposePanel().apply {
            setContent {
                ProcessingTheme {
                    val locale = LocalLocale.current
                    this@PDEWindow.title = locale[titleKey]
                    LaunchedEffect(locale){
                        this@PDEWindow.pack()
                        this@PDEWindow.setLocationRelativeTo(null)
                    }

                    Box(modifier = Modifier
                        .padding(top = if (mac && !fullWindowContent) 22.dp else 0.dp)
                    ) {
                        content()

                    }
                }
            }

            this@PDEWindow.add(this)
        }
        pack()
        background = java.awt.Color.white
        setLocationRelativeTo(null)
        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ESCAPE) this@PDEWindow.dispose()
            }
        })
        isResizable = false
        isVisible = true
        requestFocus()
    }
}

fun pdeapplication(titleKey: String = "", fullWindowContent: Boolean = false,content: @Composable () -> Unit){
    application {
        val windowState = rememberWindowState(
            size = DpSize.Unspecified,
            position = WindowPosition(Alignment.Center)
        )
        ProcessingTheme {
            val locale = LocalLocale.current
            val mac = SystemInfo.isMacFullWindowContentSupported
            Window(onCloseRequest = ::exitApplication, state = windowState, title = locale[titleKey]) {
                window.rootPane.apply {
                    putClientProperty("apple.awt.fullWindowContent", mac)
                    putClientProperty("apple.awt.transparentTitleBar", mac)
                }
                LaunchedEffect(locale){
                    window.pack()
                    window.setLocationRelativeTo(null)
                }
                Surface(color = colors.background) {
                    Box(modifier = Modifier
                        .padding(top = if (mac && !fullWindowContent) 22.dp else 0.dp)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}
