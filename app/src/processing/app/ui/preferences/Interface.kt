package processing.app.ui.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import processing.app.Language
import processing.app.LocalPreferences
import processing.app.Preferences
import processing.app.ui.PDEPreference
import processing.app.ui.PDEPreferencePane
import processing.app.ui.PDEPreferences
import processing.app.ui.Toolkit
import processing.app.ui.preferences.General.Companion.general
import processing.app.ui.theme.LocalLocale
import java.util.*

class Interface {
    companion object{
        val interfaceAndFonts = PDEPreferencePane(
            nameKey = "preferences.pane.interface",
            icon = {
                Icon(Icons.Default.Brush, contentDescription = "Interface")
            },
            after = general
        )

        @OptIn(ExperimentalMaterial3Api::class)
        fun register() {
            PDEPreferences.register(
                PDEPreference(
                    key = "language",
                    descriptionKey = "preferences.language",
                    pane = interfaceAndFonts,
                    control = { preference, updatePreference ->
                        val locale = LocalLocale.current
                        val showOptions = remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = {
                                showOptions.value = true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(locale.locale.displayName)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        languagesDropdown(showOptions)
                    }
                ),
                PDEPreference(
                    key = "editor.input_method_support",
                    descriptionKey = "preferences.enable_complex_text",
                    pane = interfaceAndFonts,
                    control = { preference, updatePreference ->
                        val enabled = preference?.toBoolean() ?: true
                        Switch(
                            checked = enabled,
                            onCheckedChange = {
                                updatePreference(it.toString())
                            }
                        )
                    }
                )
            )
            PDEPreferences.register(
                PDEPreference(
                    key = "editor.theme",
                    descriptionKey = "preferences.editor.theme",
                    pane = interfaceAndFonts,
                    control = { preference, updatePreference ->
                        val locale = LocalLocale.current
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InputChip(
                                selected = (preference ?: "") == "",
                                onClick = {
                                    updatePreference("")
                                },
                                label = {
                                    Text(locale["preferences.editor.theme.system"])
                                }
                            )
                            InputChip(
                                selected = preference == "dark",
                                onClick = {
                                    updatePreference("dark")
                                },
                                label = {
                                    Text(locale["preferences.editor.theme.dark"])
                                }
                            )
                            InputChip(
                                selected = preference == "light",
                                onClick = {
                                    updatePreference("light")
                                },
                                label = {
                                    Text(locale["preferences.editor.theme.light"])
                                }
                            )
                        }
                    }
                ),
                PDEPreference(
                    key = "editor.zoom",
                    descriptionKey = "preferences.interface_scale",
                    pane = interfaceAndFonts,
                    control = { preference, updatePreference ->
                        val range = 100f..300f

                        val prefs = LocalPreferences.current
                        var currentZoom by remember(preference) {
                            mutableStateOf(
                                preference
                                    ?.replace("%", "")
                                    ?.toFloatOrNull()
                                    ?: range.start
                            )
                        }
                        val automatic = currentZoom == range.start
                        val zoomPerc = "${currentZoom.toInt()}%"
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 200.dp)
                            ) {
                                Text(
                                    text = if (automatic) "Auto" else zoomPerc,
                                )
                                Slider(
                                    value = currentZoom,
                                    onValueChange = {
                                        currentZoom = it
                                    },
                                    onValueChangeFinished = {
                                        prefs["editor.zoom.auto"] = automatic
                                        updatePreference(zoomPerc)
                                    },
                                    valueRange = range,
                                    steps = 3
                                )
                            }
                        }
                    }
                )
            )

            PDEPreferences.register(
                PDEPreference(
                    key = "editor.font.family",
                    descriptionKey = "preferences.editor_and_console_font",
                    pane = interfaceAndFonts,
                    control = { preference, updatePreference ->
                        var showOptions by remember { mutableStateOf(false) }
                        val families =
                            if (Preferences.isInitialized()) Toolkit.getMonoFontFamilies() else arrayOf("Monospaced")
                        OutlinedButton(
                            onClick = {
                                showOptions = true
                            },
                            modifier = Modifier.width(200.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(preference ?: families.firstOrNull().orEmpty())
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showOptions,
                            onDismissRequest = {
                                showOptions = false
                            },
                        ) {
                            families.forEach { family ->
                                DropdownMenuItem(
                                    text = { Text(family) },
                                    onClick = {
                                        updatePreference(family)
                                        showOptions = false
                                    }
                                )
                            }

                        }
                    }
                ),
                PDEPreference(
                    key = "editor.font.size",
                    descriptionKey = "preferences.editor_font_size",
                    pane = interfaceAndFonts,
                    control = { preference, updatePreference ->
                        Column(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                        ) {
                            Text(
                                text = "${preference ?: "12"} pt",
                                modifier = Modifier.width(120.dp)
                            )
                            Slider(
                                value = (preference ?: "12").toFloat(),
                                onValueChange = { updatePreference(it.toInt().toString()) },
                                valueRange = 10f..48f,
                                steps = 18
                            )
                        }
                    }
                ), PDEPreference(
                    key = "console.font.size",
                    descriptionKey = "preferences.console_font_size",
                    pane = interfaceAndFonts,
                    control = { preference, updatePreference ->
                        Column(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                        ) {
                            Text(
                                text = "${preference ?: "12"} pt",
                                modifier = Modifier.width(120.dp)
                            )
                            Slider(
                                value = (preference ?: "12").toFloat(),
                                onValueChange = { updatePreference(it.toInt().toString()) },
                                valueRange = 10f..48f,
                                steps = 18,
                            )
                        }
                    }
                )
            )
        }

        @Composable
        fun languagesDropdown(showOptions: MutableState<Boolean>) {
            val locale = LocalLocale.current
            val languages = if (Preferences.isInitialized()) Language.getLanguages() else mapOf("en" to "English")
            DropdownMenu(
                expanded = showOptions.value,
                onDismissRequest = {
                    showOptions.value = false
                },
            ) {
                languages.forEach { family ->
                    DropdownMenuItem(
                        text = { Text(family.value) },
                        onClick = {
                            locale.set(Locale(family.key))
                            showOptions.value = false
                        }
                    )
                }
            }
        }
    }
}