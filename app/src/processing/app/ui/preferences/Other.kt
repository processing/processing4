package processing.app.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import processing.app.LocalPreferences
import processing.app.ui.LocalPreferenceGroups
import processing.app.ui.PDEPreference
import processing.app.ui.PDEPreferenceGroup
import processing.app.ui.PDEPreferences
import processing.app.ui.preferences.Interface.Companion.interfaceAndFonts
import processing.app.ui.theme.LocalLocale

class Other {
    companion object{
        val other = PDEPreferenceGroup(
            name = "Other",
            icon = {
                Icon(Icons.Default.Map, contentDescription = "A map icon")
            },
            after = interfaceAndFonts
        )
        fun register() {
            PDEPreferences.register(
                PDEPreference(
                    key = "other",
                    descriptionKey = "preferences.other",
                    group = other,
                    noPadding = true,
                    control = { _, _ ->
                        val prefs = LocalPreferences.current
                        val groups = LocalPreferenceGroups.current
                        val restPrefs = remember {
                            val keys = prefs.keys.mapNotNull { it as? String }
                            val existing = groups.values.flatten().map { it.key }
                            keys.filter { it !in existing }.sorted()
                        }
                        val locale = LocalLocale.current

                        for(prefKey in restPrefs){
                            val value = prefs[prefKey]
                            Row (
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ){
                                Text(
                                    text = locale[prefKey],
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                                TextField(value ?: "", onValueChange = {
                                    prefs[prefKey] = it
                                })
                            }
                        }

                    }
                )
            )
        }
    }
}