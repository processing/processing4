package processing.app.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import processing.app.Preferences
import processing.app.SketchName
import processing.app.ui.PDEPreference
import processing.app.ui.PDEPreferenceGroup
import processing.app.ui.PDEPreferences


class General {
    companion object{
        val general = PDEPreferenceGroup(
            name = "General",
            icon = {
                Icon(Icons.Default.Settings, contentDescription = "A settings icon")
            }
        )

        fun register() {
            PDEPreferences.register(
                PDEPreference(
                    key = "sketchbook.path.four",
                    descriptionKey = "preferences.sketchbook_location",
                    group = general,
                    control = { preference, updatePreference ->
                        Row (
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            TextField(
                                value = preference ?: "",
                                onValueChange = {
                                    updatePreference(it)
                                }
                            )
                            Button(
                                onClick = {

                                }
                            ) {
                                Text("Browse")
                            }
                        }
                    }
                )
            )
            PDEPreferences.register(
                PDEPreference(
                    key = "sketch.name.approach",
                    descriptionKey = "preferences.sketch_naming",
                    group = general,
                    control = { preference, updatePreference ->
                        Row{
                            for (option in if(Preferences.isInitialized()) SketchName.getOptions() else arrayOf(
                                "timestamp",
                                "untitled",
                                "custom"
                            )) {
                                FilterChip(
                                    selected = preference == option,
                                    onClick = {
                                        updatePreference(option)
                                    },
                                    label = {
                                        Text(option)
                                    },
                                    modifier = Modifier.padding(4.dp),
                                )
                            }
                        }
                    }
                )
            )
            PDEPreferences.register(
                PDEPreference(
                    key = "update.check",
                    descriptionKey = "preferences.check_for_updates_on_startup",
                    group = general,
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
                    descriptionKey = "preferences.show_welcome_screen_on_startup",
                    group = general,
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
        }
    }
}