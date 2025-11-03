package processing.app.ui.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import processing.app.ui.PDEPreference
import processing.app.ui.PDEPreferencePane
import processing.app.ui.PDEPreferences
import processing.app.ui.preferences.Interface.Companion.interfaceAndFonts

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
                    control = { preference, setPreference ->
                        Switch(
                            checked = preference?.toBoolean() ?: false,
                            onCheckedChange = { setPreference(it.toString()) }
                        )
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