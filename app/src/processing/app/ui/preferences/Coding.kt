package processing.app.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import processing.app.ui.PDEPreference
import processing.app.ui.PDEPreferencePane
import processing.app.ui.PDEPreferences
import processing.app.ui.preferences.Interface.Companion.interfaceAndFonts
import processing.app.ui.theme.LocalLocale

class Coding {
    companion object {
        val coding = PDEPreferencePane(
            nameKey = "preferences.pane.editor",
            icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
            after = interfaceAndFonts,
        )

        fun register() {
            PDEPreferences.register(
                PDEPreference(
                    key = "pdex.errorCheckEnabled",
                    descriptionKey = "preferences.continuously_check",
                    pane = coding,
                    control = { preference, setPreference ->
                        Switch(
                            checked = preference?.toBoolean() ?: false,
                            onCheckedChange = { setPreference(it.toString()) }
                        )
                    }
                ),
                PDEPreference(
                    key = "pdex.warningsEnabled",
                    descriptionKey = "preferences.show_warnings",
                    pane = coding,
                    control = { preference, setPreference ->
                        Switch(
                            checked = preference?.toBoolean() ?: false,
                            onCheckedChange = { setPreference(it.toString()) }
                        )
                    }
                ),
                PDEPreference(
                    key = "pdex.completion",
                    descriptionKey = "preferences.code_completion",
                    pane = coding,
                    noTitle = true,
                    control = { preference, setPreference ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val locale = LocalLocale.current
                            Text(
                                text = locale["preferences.code_completion"] + " Ctrl-" + locale["preferences.cmd_space"],
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = preference?.toBoolean() ?: false,
                                onCheckedChange = { setPreference(it.toString()) }
                            )
                        }
                    }
                ),
                PDEPreference(
                    key = "pdex.suggest.imports",
                    descriptionKey = "preferences.suggest_imports",
                    pane = coding,
                    control = { preference, setPreference ->
                        Switch(
                            checked = preference?.toBoolean() ?: false,
                            onCheckedChange = { setPreference(it.toString()) }
                        )
                    }
                )
            )
        }
    }
}