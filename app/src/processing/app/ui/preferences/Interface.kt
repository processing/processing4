package processing.app.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import processing.app.Language
import processing.app.Preferences
import processing.app.ui.PDEPreference
import processing.app.ui.PDEPreferenceGroup
import processing.app.ui.PDEPreferences
import processing.app.ui.Toolkit
import processing.app.ui.preferences.General.Companion.general
import processing.app.ui.theme.LocalLocale
import java.util.Locale

class Interface {
    companion object{
        val interfaceAndFonts = PDEPreferenceGroup(
            name = "Interface",
            icon = {
                Icon(Icons.Default.TextIncrease, contentDescription = "Interface")
            },
            after = general
        )

        fun register() {
            PDEPreferences.register(PDEPreference(
                key = "language",
                descriptionKey = "preferences.language",
                group = interfaceAndFonts,
                control = { preference, updatePreference ->
                    val locale = LocalLocale.current
                    val showOptions = remember { mutableStateOf(false) }
                    TextField(
                        value = locale.locale.displayName,
                        readOnly = true,
                        onValueChange = {  },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Select Font Family",
                                modifier = Modifier
                                    .clickable{
                                        showOptions.value = true
                                    }
                            )
                        }
                    )
                    languagesDropdown(showOptions)
                }
            ))

            PDEPreferences.register(
                PDEPreference(
                    key = "editor.font.family",
                    descriptionKey = "preferences.editor_and_console_font",
                    group = interfaceAndFonts,
                    control = { preference, updatePreference ->
                        var showOptions by remember { mutableStateOf(false) }
                        val families = if(Preferences.isInitialized()) Toolkit.getMonoFontFamilies() else arrayOf("Monospaced")
                        TextField(
                            value = preference ?: families.firstOrNull().orEmpty(),
                            readOnly = true,
                            onValueChange = { updatePreference (it) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Font Family",
                                    modifier = Modifier
                                        .clickable{
                                            showOptions = true
                                        }
                                )
                            }
                        )
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
                )
            )

            PDEPreferences.register(PDEPreference(
                key = "editor.font.size",
                descriptionKey = "preferences.editor_font_size",
                group = interfaceAndFonts,
                control = { preference, updatePreference ->
                    Column {
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
            ))
            PDEPreferences.register(PDEPreference(
                key = "console.font.size",
                descriptionKey = "preferences.console_font_size",
                group = interfaceAndFonts,
                control = { preference, updatePreference ->
                    Column {
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
            ))
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