package processing.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import processing.app.LocalPreferences
import processing.app.ui.PDEPreferences.Companion.preferences
import processing.app.ui.preferences.General
import processing.app.ui.preferences.Interface
import processing.app.ui.preferences.Other
import processing.app.ui.theme.LocalLocale
import processing.app.ui.theme.PDESwingWindow
import processing.app.ui.theme.PDETheme
import java.awt.Dimension
import javax.swing.SwingUtilities

val LocalPreferenceGroups = compositionLocalOf<MutableMap<PDEPreferenceGroup, List<PDEPreference>>> {
    error("No Preference Groups Set")
}

class PDEPreferences {
    companion object{
        val groups = mutableStateMapOf<PDEPreferenceGroup, List<PDEPreference>>()
        fun register(preference: PDEPreference) {
            val list = groups[preference.group]?.toMutableList() ?: mutableListOf()
            list.add(preference)
            groups[preference.group] = list
        }
        init{
            General.register()
            Interface.register()
            Other.register()
        }

        /**
         * Composable function to display the preferences UI.
         */
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun preferences(){
            var visible by remember { mutableStateOf(groups) }
            val sortedGroups = remember {
                val keys = visible.keys
                keys.toSortedSet {
                        a, b ->
                    when {
                        a.after == b -> 1
                        b.after == a -> -1
                        else -> a.name.compareTo(b.name)
                    }
                }
            }
            var selected by remember { mutableStateOf(sortedGroups.first()) }
            CompositionLocalProvider(
                LocalPreferenceGroups provides visible
            ) {
                Row {
                    NavigationRail(
                        header = {
                            Text(
                                "Settings",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 42.dp)
                            )
                        },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 200.dp)
                    ) {

                        for (group in sortedGroups) {
                            NavigationRailItem(
                                selected = selected == group,
                                enabled = visible.keys.contains(group),
                                onClick = {
                                    selected = group
                                },
                                icon = {
                                    group.icon()
                                },
                                label = {
                                    Text(group.name)
                                }
                            )
                        }
                    }
                    Box(modifier = Modifier.padding(top = 42.dp)) {
                        Column(modifier = Modifier
                            .fillMaxSize()
                        ) {
                            var query by remember { mutableStateOf("") }
                            val locale = LocalLocale.current
                            LaunchedEffect(query){

                                snapshotFlow { query }
                                    .debounce(100)
                                    .collect{
                                        if(it.isBlank()){
                                            visible = groups
                                            return@collect
                                        }
                                        val filtered = mutableStateMapOf<PDEPreferenceGroup, List<PDEPreference>>()
                                        for((group, preferences) in groups){
                                            val matching = preferences.filter { preference ->
                                                if(preference.key == "other"){
                                                    return@filter true
                                                }
                                                if(preference.key.contains(it, ignoreCase = true)){
                                                    return@filter true
                                                }
                                                val description = locale[preference.descriptionKey]
                                                description.contains(it, ignoreCase = true)
                                            }
                                            if(matching.isNotEmpty()){
                                                filtered[group] = matching
                                            }
                                        }
                                        visible = filtered
                                    }

                            }
                            SearchBar(
                                inputField = {
                                    SearchBarDefaults.InputField(
                                        query = query,
                                        onQueryChange = {
                                            query = it
                                        },
                                        onSearch = {

                                        },
                                        expanded = false,
                                        onExpandedChange = {  },
                                        placeholder = { Text("Search") }
                                    )
                                },
                                expanded = false,
                                onExpandedChange = {},
                                modifier = Modifier.align(Alignment.End).padding(16.dp)
                            ) {

                            }

                            val preferences = visible[selected] ?: emptyList()
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                items(preferences){ preference ->
                                    preference.showControl()
                                }
                            }
                        }
                    }
                }
            }
        }



        @JvmStatic
        fun main(args: Array<String>) {
            application {
                Window(onCloseRequest = ::exitApplication){
                    remember{
                        window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                        window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                    }
                    PDETheme(darkTheme = true) {
                        preferences()
                    }
                }
                Window(onCloseRequest = ::exitApplication){
                    remember{
                        window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                        window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                    }
                    PDETheme(darkTheme = false) {
                        preferences()
                    }
                }
            }
        }
    }
}

/**
 * Data class representing a single preference in the preferences system.
 *
 * Usage:
 * ```
 * PDEPreferences.register(
 *     PDEPreference(
 *         key = "preference.key",
 *         descriptionKey = "preference.description",
 *         group = somePreferenceGroup,
 *         control = { preference, updatePreference ->
 *             // Composable UI to modify the preference
 *         }
 *     )
 * )
 * ```
 */
data class PDEPreference(
    /**
     * The key in the preferences file used to store this preference.
     */
    val key: String,
    /**
     * The key for the description of this preference, used for localization.
     */
    val descriptionKey: String,
    /**
     * The group this preference belongs to.
     */
    val group: PDEPreferenceGroup,
    /**
     * A Composable function that defines the control used to modify this preference.
     * It takes the current preference value and a function to update the preference.
     */
    val control: @Composable (preference: String?, updatePreference: (newValue: String) -> Unit) -> Unit = { preference, updatePreference ->  },

    /**
     * If true, no padding will be applied around this preference's UI.
     */
    val noPadding: Boolean = false,
)

/**
 * Composable function to display the preference's description and control.
 */
@Composable
private fun PDEPreference.showControl() {
    val locale = LocalLocale.current
    val prefs = LocalPreferences.current
    Text(
        text = locale[descriptionKey],
        modifier = Modifier.padding(horizontal = 20.dp),
        style = MaterialTheme.typography.titleMedium
    )
    val show = @Composable {
        control(prefs[key]) { newValue ->
            prefs[key] = newValue
        }
    }

    if(noPadding){
        show()
    }else{
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            show()
        }
    }
}

/**
 * Data class representing a group of preferences.
 */
data class PDEPreferenceGroup(
    /**
     * The name of this group.
     */
    val name: String,
    /**
     * The icon representing this group.
     */
    val icon: @Composable () -> Unit,
    /**
     * The group that comes before this one in the list.
     */
    val after: PDEPreferenceGroup? = null,
)

fun show(){
    SwingUtilities.invokeLater {
        PDESwingWindow(
            titleKey = "preferences",
            fullWindowContent = true,
            size = Dimension(800, 600)
        ) {
            PDETheme {
                preferences()
            }
        }
    }
}