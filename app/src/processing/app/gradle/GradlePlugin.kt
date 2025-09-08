package processing.app.gradle

import androidx.compose.runtime.mutableStateListOf
import processing.app.Base
import java.nio.file.Path

data class GradlePlugin(
    val name: String,
    val description: String,
    val repository: Path?,
    val id: String,
    val version: String){
    companion object{
        const val PROPERTIES_KEY = "sketch.plugins"
        val plugins = mutableStateListOf<GradlePlugin>(
            GradlePlugin("Hot Reload", "Automatically apply changes in your sketch upon saving", null, "org.processing.java.hotreload", Base.getVersionName()),
            GradlePlugin("Android","Run your sketch on an Android device", null, "org.processing.android", Base.getVersionName()),
        )
    }
}