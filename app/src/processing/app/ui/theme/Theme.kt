package processing.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import darkScheme
import lightScheme
import processing.app.PreferencesProvider

/**
 * Processing Theme for Jetpack Compose Desktop
 * Based on Material3
 *
 * Makes Material3 components follow Processing color scheme and typography
 * We experimented with using the material3 theme builder, but it made it look too Android-y
 * So we defined our own color scheme and typography based on Processing design guidelines
 *
 * This composable also provides Preferences and Locale context to all child composables
 *
 * Also, important: sets a default density of 1.25 for better scaling on desktop screens, [LocalDensity]
 *
 * Usage:
 * ```
 * PDETheme {
 *    val pref = LocalPreferences.current
 *    val locale = LocalLocale.current
 *    ...
 *    // Your composables here
 * }
 * ```
 *
 * @param darkTheme Whether to use dark theme or light theme. Defaults to system setting.
 */
@Composable
fun PDETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
){
    PreferencesProvider {
        LocaleProvider {
            MaterialTheme(
                colorScheme = if(darkTheme) darkScheme else lightScheme,
                typography = PDETypography
            ){
                Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface,
//                        LocalDensity provides Density(1.25f, 1.25f),
                        content = content
                    )
                }
            }
        }
    }
}

/**
 * Simple app to preview the Processing Theme components
 * Includes buttons, text fields, checkboxes, sliders, etc.
 * Run by executing the main() function by clicking the green arrow next to it in intelliJ IDEA
 */
fun main() {
    application {
        val windowState = rememberWindowState(
            size = DpSize(800.dp, 600.dp),
            position = WindowPosition(Alignment.Center)
        )
        var darkTheme by remember { mutableStateOf(false) }
        Window(onCloseRequest = ::exitApplication, state = windowState, title = "Processing Theme") {
            PDETheme(darkTheme = darkTheme) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Processing Theme Components", style = MaterialTheme.typography.titleLarge)
                    Card {
                        Row {
                            Checkbox(darkTheme, onCheckedChange = { darkTheme = !darkTheme })
                            Text(
                                "Dark Theme",
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                    val scrollable = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollable),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ComponentPreview("Colors") {
                            val colors = listOf<Triple<String, Color, Color>>(
                                Triple("Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary),
                                Triple("Secondary", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary),
                                Triple("Tertiary", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary),
                                Triple("Primary Container", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer),
                                Triple("Secondary Container", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer),
                                Triple("Tertiary Container", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer),
                                Triple("Error Container", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer),
                                Triple("Background", MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.onBackground),
                                Triple("Surface", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface),
                                Triple("Surface Variant", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant),
                                Triple("Error", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError),

                                Triple("Surface Lowest", MaterialTheme.colorScheme.surfaceContainerLowest, MaterialTheme.colorScheme.onSurface),
                                Triple("Surface Low", MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.colorScheme.onSurface),
                                Triple("Surface", MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.colorScheme.onSurface),
                                Triple("Surface High", MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurface),
                                Triple("Surface Highest", MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurface),
                            )
                            Column {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                                    Button(
//                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
//                                        onClick = {}) {
//                                        Text("Primary", color = MaterialTheme.colorScheme.onPrimary)
//                                    }
//                                    Button(
//                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
//                                        onClick = {}) {
//                                        Text("Secondary", color = MaterialTheme.colorScheme.onSecondary)
//                                    }
//                                    Button(
//                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
//                                        onClick = {}) {
//                                        Text("Tertiary", color = MaterialTheme.colorScheme.onTertiary)
//                                    }
                                    val section = colors.subList(0,3)
                                    for((name, color, onColor) in section){
                                        Button(
                                            colors = ButtonDefaults.buttonColors(containerColor = color),
                                            onClick = {}) {
                                            Text(name, color = onColor)
                                            }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    val section = colors.subList(3,7)
                                    for((name, color, onColor) in section){
                                        Button(
                                            colors = ButtonDefaults.buttonColors(containerColor = color),
                                            onClick = {}) {
                                            Text(name, color = onColor)
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    val section = colors.subList(7,11)
                                    for((name, color, onColor) in section){
                                        Button(
                                            colors = ButtonDefaults.buttonColors(containerColor = color),
                                            onClick = {}) {
                                            Text(name, color = onColor)
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    val section = colors.subList(11, 16)
                                    for ((name, color, onColor) in section) {
                                        Button(
                                            colors = ButtonDefaults.buttonColors(containerColor = color),
                                            onClick = {}) {
                                            Text(name, color = onColor)
                                        }
                                    }
                                }
                            }
                        }
                        ComponentPreview("Text & Fonts") {
                            Column {
                                Text("displayLarge", style = MaterialTheme.typography.displayLarge)
                                Text("displayMedium", style = MaterialTheme.typography.displayMedium)
                                Text("displaySmall", style = MaterialTheme.typography.displaySmall)

                                Text("headlineLarge", style = MaterialTheme.typography.headlineLarge)
                                Text("headlineMedium", style = MaterialTheme.typography.headlineMedium)
                                Text("headlineSmall", style = MaterialTheme.typography.headlineSmall)

                                Text("titleLarge", style = MaterialTheme.typography.titleLarge)
                                Text("titleMedium", style = MaterialTheme.typography.titleMedium)
                                Text("titleSmall", style = MaterialTheme.typography.titleSmall)

                                Text("bodyLarge", style = MaterialTheme.typography.bodyLarge)
                                Text("bodyMedium", style = MaterialTheme.typography.bodyMedium)
                                Text("bodySmall", style = MaterialTheme.typography.bodySmall)

                                Text("labelLarge", style = MaterialTheme.typography.labelLarge)
                                Text("labelMedium", style = MaterialTheme.typography.labelMedium)
                                Text("labelSmall", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        ComponentPreview("Buttons") {
                            Button(onClick = {}) {
                                Text("Filled")
                            }
                            Button(onClick = {}, enabled = false) {
                                Text("Disabled")
                            }
                            OutlinedButton(onClick = {}) {
                                Text("Outlined")
                            }
                            TextButton(onClick = {}) {
                                Text("Text")
                            }
                        }
                        ComponentPreview("Icon Buttons") {
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Map, contentDescription = "Icon Button")
                            }
                        }
                        ComponentPreview("Chip") {
                            AssistChip(onClick = {}, label = {
                                Text("Assist Chip")
                            })
                            FilterChip(selected = false, onClick = {}, label = {
                                Text("Filter not Selected")
                            })
                            FilterChip(selected = true, onClick = {}, label = {
                                Text("Filter Selected")
                            })
                        }
                        ComponentPreview("Progress Indicator") {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)){
                                CircularProgressIndicator()
                                LinearProgressIndicator()
                            }
                        }
                        ComponentPreview("Radio Button") {
                            var state by remember { mutableStateOf(true) }
                            RadioButton(!state, onClick = { state = false })
                            RadioButton(state, onClick = { state = true })

                        }
                        ComponentPreview("Checkbox") {
                            var state by remember { mutableStateOf(true) }
                            Checkbox(state, onCheckedChange = { state = it })
                            Checkbox(!state, onCheckedChange = { state = !it })
                            Checkbox(state, onCheckedChange = {}, enabled = false)
                            TriStateCheckbox(ToggleableState.Indeterminate, onClick = {})
                        }
                        ComponentPreview("Switch") {
                            var state by remember { mutableStateOf(true) }
                            Switch(state, onCheckedChange = { state = it })
                            Switch(!state, enabled = false, onCheckedChange = { state = it })
                        }
                        ComponentPreview("Slider") {
                            Column{
                                var state by remember { mutableStateOf(0.5f) }
                                Slider(state, onValueChange = { state = it })
                                var rangeState by remember { mutableStateOf(0.25f..0.75f) }
                                RangeSlider(rangeState, onValueChange = { rangeState = it })
                            }

                        }
                        ComponentPreview("Badge") {
                            IconButton(onClick = {}) {
                                BadgedBox(badge = { Badge() }) {
                                    Icon(Icons.Default.Map, contentDescription = "Icon with Badge")
                                }
                            }
                        }
                        ComponentPreview("Number Field") {
                            var number by remember { mutableStateOf("123") }
                            TextField(number, onValueChange = {
                                if(it.all { char -> char.isDigit() }) {
                                    number = it
                                }
                            }, label = { Text("Number Field") })

                        }
                        ComponentPreview("Text Field") {
                            Row {
                                var text by remember { mutableStateOf("Text Field") }
                                TextField(text, onValueChange = { text = it })
                            }
                            var text by remember { mutableStateOf("Outlined Text Field") }
                            OutlinedTextField(text, onValueChange = { text = it})
                        }
                        ComponentPreview("Dropdown Menu") {
                            var show by remember { mutableStateOf(false) }
                            AssistChip(
                                onClick = { show = true },
                                label = { Text("Show Menu") }
                            )
                            DropdownMenu(
                                expanded = show,
                                onDismissRequest = {
                                    show = false
                                },
                            ) {
                                DropdownMenuItem(onClick = { show = false }, text = {
                                    Text("Menu Item 1", modifier = Modifier.padding(8.dp))
                                })
                                DropdownMenuItem(onClick = { show = false }, text = {
                                    Text("Menu Item 2", modifier = Modifier.padding(8.dp))
                                })
                                DropdownMenuItem(onClick = { show = false }, text = {
                                    Text("Menu Item 3", modifier = Modifier.padding(8.dp))
                                })
                            }


                        }

                        ComponentPreview("Card") {
                            Card{
                                Text("Hello, Tabs!", modifier = Modifier.padding(20.dp))
                            }
                        }

                        ComponentPreview("Scrollable View") {

                        }



                        ComponentPreview("Tabs") {

                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentPreview(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge)
        HorizontalDivider()
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            content()
        }
        HorizontalDivider()
    }
}