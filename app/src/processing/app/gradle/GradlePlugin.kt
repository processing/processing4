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
        val group = System.getProperty("processing.group", "org.processing")
        val plugins = mutableStateListOf(
            GradlePlugin(
                "Hot Reload",
                "Automatically apply changes in your sketch upon saving",
                null,
                "$group.java.hotreload",
                Base.getVersionName()
            ),
            GradlePlugin(
                "Android",
                "Run your sketch on an Android device",
                null,
                "$group.android",
                Base.getVersionName()
            ),
        )
    }
}