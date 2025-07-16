package processing.app.gradle

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposePanel
import javax.swing.JPanel

class GradleSettings : JPanel() {
    init{
        val compose = ComposePanel()
        compose.setContent {
            Panel()
        }
        this.add(compose)
    }
    companion object{
        @Composable
        fun Panel(){
            Text("Gradle settings will be here soon")
        }

    }
}