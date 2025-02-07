package processing.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.unit.dp
import com.formdev.flatlaf.util.SystemInfo
import processing.app.Base
import processing.app.contrib.ui.contributionsManager
import processing.app.ui.theme.Locale
import java.io.IOException
import javax.swing.JFrame
import javax.swing.SwingUtilities

class Welcome @Throws(IOException::class) constructor(base: Base) {
    init {
        SwingUtilities.invokeLater {


            JFrame(Locale()["menu.help.welcome"]).apply{
                val mac = SystemInfo.isMacFullWindowContentSupported

                rootPane.putClientProperty("apple.awt.transparentTitleBar", mac)
                rootPane.putClientProperty("apple.awt.fullWindowContent", mac)

                defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
                add(ComposePanel().apply {
                    setContent {
                        Box(modifier = Modifier.padding(top = if (mac) 22.dp else 0.dp)) {
                            welcome()
                        }
                    }
                })

                pack()

                setLocationRelativeTo(null)

                isVisible = true

            }
        }
    }

    @Composable
    fun welcome() {
        Box(modifier = Modifier.sizeIn(815.dp, 450.dp)){

        }
    }
}