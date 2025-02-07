package processing.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import processing.app.Base
import processing.app.ui.theme.PDEWindow
import processing.app.ui.theme.pdeapplication
import java.io.IOException
import javax.swing.SwingUtilities

class Welcome @Throws(IOException::class) constructor(base: Base) {
    init {
        SwingUtilities.invokeLater {
            PDEWindow("menu.help.welcome") {
                welcome()
            }
        }
    }
    companion object {
        @Composable
        fun welcome() {
            Box(modifier = Modifier.sizeIn(815.dp, 450.dp)){

            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            pdeapplication("menu.help.welcome") {
                welcome()
            }
        }
    }
}