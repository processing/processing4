package processing.app.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import processing.app.Preferences
import processing.app.SketchName
import processing.app.ui.EditorFooter.copyDebugInformationToClipboard
import processing.app.ui.PDEPreference
import processing.app.ui.PDEPreferencePane
import processing.app.ui.PDEPreferences
import processing.app.ui.theme.LocalLocale
import processing.awt.ShimAWT.selectFolder
import java.io.File


class General {
    companion object{
        val general = PDEPreferencePane(
            nameKey = "preferences.pane.general",
            icon = {
                Icon(Icons.Default.Settings, contentDescription = "General Preferences")
            }
        )

        fun register() {
            PDEPreferences.register(
                PDEPreference(
                    key = "sketchbook.path.four",
                    descriptionKey = "preferences.sketchbook_location",
                    pane = general,
                    noTitle = true,
                    control = { preference, updatePreference ->
                        val locale = LocalLocale.current
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(locale["preferences.sketchbook_location"]) },
                            value = preference ?: "",
                            singleLine = true,
                            onValueChange = {
                                updatePreference(it)
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .clickable {
                                            selectFolder(
                                                locale["preferences.sketchbook_location.popup"],
                                                File(preference ?: "")
                                            ) { selectedFile: File? ->
                                                if (selectedFile != null) {
                                                    updatePreference(selectedFile.absolutePath)
                                                }
                                            }
                                        }
                                )
                            }
                        )
                    }
                ),
                PDEPreference(
                    key = "sketch.name.approach",
                    descriptionKey = "preferences.sketch_naming",
                    pane = general,
                    control = { preference, updatePreference ->
                        Column {
                            val options = if (Preferences.isInitialized()) SketchName.getOptions() else arrayOf(
                                "timestamp",
                                "untitled",
                                "custom"
                            )
                            options.toList().chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { option ->
                                        InputChip(
                                            selected = preference == option,
                                            onClick = {
                                                updatePreference(option)
                                            },
                                            label = {
                                                Text(option)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                ),
                PDEPreference(
                    key = "editor.sync_folder_and_filename",
                    labelKey = "preferences.experimental",
                    descriptionKey = "preferences.sync_folder_and_filename",
                    pane = general,
                    control = { preference, updatePreference ->
                        Switch(
                            checked = preference.toBoolean(),
                            onCheckedChange = {
                                updatePreference(it.toString())
                            }
                        )
                    }
                )
            )
            PDEPreferences.register(
                PDEPreference(
                    key = "update.check",
                    descriptionKey = "preferences.update_check",
                    pane = general,
                    control = { preference, updatePreference ->
                        Switch(
                            checked = preference.toBoolean(),
                            onCheckedChange = {
                                updatePreference(it.toString())
                            }
                        )
                    }
                )
            )
            PDEPreferences.register(
                PDEPreference(
                    key = "welcome.four.show",
                    descriptionKey = "preferences.show_welcome_screen",
                    pane = general,
                    control = { preference, updatePreference ->
                        Switch(
                            checked = preference.toBoolean(),
                            onCheckedChange = {
                                updatePreference(it.toString())
                            }
                        )
                    }
                )
            )
            PDEPreferences.register(
                PDEPreference(
                    key = "welcome.show",
                    descriptionKey = "preferences.diagnostics",
                    pane = general,
                    control = { preference, updatePreference ->
                        var copied by remember { mutableStateOf(false) }
                        LaunchedEffect(copied) {
                            if (copied) {
                                delay(2000)
                                copied = false
                            }
                        }
                        Button(onClick = {
                            copyDebugInformationToClipboard()
                            copied = true

                        }) {
                            if (!copied) {
                                Text(LocalLocale.current["preferences.diagnostics.button"])
                            } else {
                                Text(LocalLocale.current["preferences.diagnostics.button.copied"])
                            }
                        }
                    }
                )
            )
        }
    }
}