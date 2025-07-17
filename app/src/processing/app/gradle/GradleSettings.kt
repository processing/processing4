package processing.app.gradle

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.formdev.flatlaf.util.SwingUtils
import com.github.ajalt.mordant.rendering.TextStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import processing.app.Language.text
import processing.app.Settings
import processing.app.Sketch
import processing.app.ui.Editor
import processing.app.ui.EditorFooter
import processing.app.ui.Theme
import processing.app.ui.theme.ProcessingTheme
import processing.app.watchFile
import java.awt.Dimension
import java.util.UUID
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.SwingUtilities

class GradleSettings{
    companion object{
        private val scope = CoroutineScope(Dispatchers.IO)

        @JvmStatic
        fun addGradleSettings(footer: EditorFooter, service: GradleService){
            val panel = ComposePanel()
            panel.setContent {
                Panel(service)
            }
            scope.launch {
                // Only add the panel to the footer when Gradle is active
                // Can be removed later when Gradle becomes the default build system
                snapshotFlow { service.active.value }
                    .collect { active ->
                        SwingUtilities.invokeLater {
                            if(active){
                                footer.addPanel(panel, text("gradle.settings"), "/lib/footer/settings")
                            }else{
                                footer.removePanel(panel)
                            }
                        }
                    }
            }
        }

        @Composable
        fun Panel(service: GradleService){
            val properties = service.sketch.value?.folder?.resolve(Sketch.PROPERTIES_NAME) ?: return
            // TODO: Rewatch again is the sketch is saved in a different location

            val changed = watchFile(properties)

            val settings = remember(changed) {Settings(properties) }

            LaunchedEffect(changed){
                /*
                If the sketch.id is not set, generate a new UUID and save it.
                We will use this key to save preferences that do not influence the sketch itself,
                so they are not code, but do influence how the sketch shows up in the editor.
                This is useful for things like favoring a sketch
                These are items that should not be shared between users/computers
                // TODO: Reset id on save-as?
                 */
                if(settings.get("sketch.id") == null){
                    // TODO: Should this watch the file or should it update a bunch on running the sketch?
                    settings.set("sketch.id", UUID.randomUUID().toString())
                    settings.save()
                }
            }
            val stateVertical = rememberScrollState(0)

            ProcessingTheme {
                Box {
                    Row(
                        modifier = Modifier
                            .background(Color(Theme.getColor("editor.line.highlight.color").rgb))
                            .padding(start = Editor.LEFT_GUTTER.dp)
                            .fillMaxSize()
                            .verticalScroll(stateVertical)
                            .padding(vertical = 4.dp)
                    ) {
                        PluginsPanel(settings)
                    }
                    VerticalScrollbar(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(8.dp)
                            .fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(stateVertical)
                    )
                }
            }
        }

        @Composable
        private fun PluginsPanel(settings: Settings) {
            // Grab the installed plugins
            val plugins = GradlePlugin.plugins

            // Grab the enabled plugins
            val pluginSetting = (settings.get(GradlePlugin.PROPERTIES_KEY) ?: "")
                .split(",")
                .map { it.trim() }
                .filter{ it.isNotEmpty() }

            // Link plugins in the settings to their installed counterparts
            val enabledPlugins = pluginSetting
                .map { id -> plugins.find { plugin -> plugin.id == id } }
            Column {
                Text(
                    text = text("gradle.settings.plugins"),
                    textAlign = TextAlign.Start,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    GradlePlugin.plugins.map { plugin ->
                        Row() {
                            Checkbox(
                                checked = enabledPlugins.contains(plugin),
                                modifier = Modifier
                                    .padding(start = 0.dp, end = 8.dp)
                                    .size(24.dp),
                                onCheckedChange = { checked ->
                                    scope.launch {
                                        // Work from the setting as we do not want to remove missing plugins
                                        val current = pluginSetting.toMutableSet()
                                        if (checked) {
                                            current.add(plugin.id)
                                        } else {
                                            current.remove(plugin.id)
                                        }
                                        settings.set(GradlePlugin.PROPERTIES_KEY, current.joinToString(","))
                                        settings.save()
                                    }
                                },
                            )
                            Column {
                                Text(
                                    text = plugin.name,
                                    textAlign = TextAlign.Start,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = plugin.description,
                                    textAlign = TextAlign.Start,
                                    fontSize = 10.sp,
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}