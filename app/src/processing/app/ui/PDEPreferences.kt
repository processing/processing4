package processing.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import processing.app.LocalPreferences
import processing.app.ReactiveProperties
import processing.app.ui.PDEPreferences.Companion.preferences
import processing.app.ui.preferences.*
import processing.app.ui.theme.*
import java.awt.Dimension
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.SwingUtilities
import javax.swing.WindowConstants


fun show() {
    SwingUtilities.invokeLater {
        PDESwingWindow(
            titleKey = "preferences",
            fullWindowContent = true,
            size = Dimension(850, 600),
            minSize = Dimension(700, 500),
        ) {
            PDETheme {
                preferences()
            }
        }
    }
}

class PDEPreferences {
    companion object{
        private val panes: PDEPreferencePanes = mutableStateMapOf()

        /**
         * Registers a new preference in the preferences' system.
         * If the preference's pane does not exist, it will be created.
         * Usage:
         * ```
         * PDEPreferences.register(
         *    PDEPreference(
         *     key = "preference.key",
         *     descriptionKey = "preference.description",
         *     pane = somePreferencePane,
         *     control = { preference, updatePreference ->
         *     // Composable UI to modify the preference
         *     }
         *   )
         * )
         * ```
         *
         * @param preferences The preference to register.
         */
        fun register(vararg preferences: PDEPreference) {
            if (preferences.map { it.pane }.toSet().size != 1) {
                throw IllegalArgumentException("All preferences must belong to the same pane")
            }
            val pane = preferences.first().pane

            val group = mutableStateListOf<PDEPreference>()
            group.addAll(preferences)

            val groups = panes[pane] as? SnapshotStateList<PDEPreferenceGroup> ?: mutableStateListOf()
            groups.add(group)
            panes[pane] = groups
        }

        /**
         * Static initializer to register default preference panes.
         */
        init{
            General.register()
            Interface.register()
            Coding.register()
            Sketches.register()
            Other.register()
        }

        /**
         * Composable function to display the preferences UI.
         */
        @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
        @Composable
        fun preferences() {
            val locale = LocalLocale.current
            var preferencesQuery by remember { mutableStateOf("") }

            Other.handleOtherPreferences(panes)

            /**
             * Filter panes based on the search query.
             */
            val panesQuierried = remember(preferencesQuery, panes) {
                if (preferencesQuery.isBlank()) {
                    panes.toMutableMap()
                } else {
                    panes.entries.associate { (pane, preferences) ->
                        val matching = preferences.map { group ->
                            group.filter { preference ->
                                val description = locale[preference.descriptionKey]
                                when {
                                    preference.key == "other" -> true
                                    preference.key.contains(preferencesQuery, ignoreCase = true) -> true
                                    description.contains(preferencesQuery, ignoreCase = true) -> true
                                    else -> false
                                }
                            }
                        }
                        pane to matching
                    }.toMutableMap()
                }
            }

            /**
             * Sort panes based on their 'after' property and name.
             */
            val panesSorted = remember(panesQuierried) {
                panesQuierried.keys.sortedWith { a, b ->
                    when {
                        a === b -> 0
                        a.after == b -> 1
                        b.after == a -> -1
                        a.after == null && b.after != null -> -1
                        b.after == null && a.after != null -> 1
                        else -> a.nameKey.compareTo(b.nameKey)
                    }
                }
            }


            /**
             * Pre-select a pane that has at least one preference to show
             * Also reset the selection when the query changes
             * */
            var selected by remember(panesQuierried) {
                mutableStateOf(panesSorted.firstOrNull() { panesQuierried[it].isNotEmpty() })
            }

            /**
             * Swapping primary and tertiary colors for the preferences window, probably should do that program-wide
             */
            val originalScheme = MaterialTheme.colorScheme
            MaterialTheme(
                colorScheme = originalScheme.copy(
                    primary = originalScheme.tertiary,
                    onPrimary = originalScheme.onTertiary,
                    primaryContainer = originalScheme.tertiaryContainer,
                    onPrimaryContainer = originalScheme.onTertiaryContainer,

                    tertiary = originalScheme.primary,
                    onTertiary = originalScheme.onPrimary,
                    tertiaryContainer = originalScheme.primaryContainer,
                    onTertiaryContainer = originalScheme.onPrimaryContainer,
                )
            ) {
                CapturePreferences {
                    Column {
                        /**
                         * Header
                         */
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 36.dp, top = 48.dp, end = 24.dp, bottom = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    text = locale["preferences"],
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                                )
                                Text(
                                    text = locale["preferences.description"],
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Spacer(modifier = Modifier.width(96.dp))
                            SearchBar(
                                modifier = Modifier
                                    .widthIn(max = 250.dp),
                                inputField = {
                                    SearchBarDefaults.InputField(
                                        query = preferencesQuery,
                                        onQueryChange = {
                                            preferencesQuery = it
                                        },
                                        onSearch = {

                                        },
                                        trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                        expanded = false,
                                        onExpandedChange = { },
                                        placeholder = { Text("Search") }
                                    )
                                },
                                expanded = false,
                                onExpandedChange = {},
                            ) {

                            }
                        }
                        HorizontalDivider()
                        Box {
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                /**
                                 * Sidebar
                                 */
                                Column(
                                    modifier = Modifier
                                        .width(IntrinsicSize.Min)
                                        .padding(30.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {

                                    for (pane in panesSorted) {
                                        val shape = RoundedCornerShape(12.dp)
                                        val isSelected = selected == pane
                                        TextButton(
                                            onClick = {
                                                selected = pane
                                            },
                                            enabled = panesQuierried[pane].isNotEmpty(),
                                            colors = if (isSelected) ButtonDefaults.buttonColors() else ButtonDefaults.textButtonColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            ),
                                            shape = shape
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 4.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                pane.icon()
                                                Text(locale[pane.nameKey])
                                            }
                                        }
                                    }
                                }

                                /**
                                 * Content Area
                                 */
                                AnimatedContent(
                                    targetState = selected,
                                    transitionSpec = {
                                        fadeIn(
                                            animationSpec = tween(300)
                                        ) togetherWith fadeOut(
                                            animationSpec = tween(300)
                                        )
                                    }
                                ) { selected ->
                                    if (selected == null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(30.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = locale["preferences.no_results"],
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        return@AnimatedContent
                                    }

                                    val groups = panesQuierried[selected] ?: emptyList()
                                    selected.showPane(groups)
                                }
                            }
                            /**
                             * Unconfirmed changes banner
                             */
                            Column(
                                modifier = Modifier.align(Alignment.BottomEnd)
                            ) {
                                val modifiable = LocalModifiablePreferences.current
                                val wiggle = remember { Animatable(0f) }
                                if (modifiable.lastCloseAttempt != null) {
                                    LaunchedEffect(modifiable.lastCloseAttempt) {
                                        wiggle.animateTo(
                                            targetValue = 50f,
                                            animationSpec = tween(100, easing = EaseOutBounce)
                                        )
                                        wiggle.animateTo(
                                            targetValue = -50f,
                                            animationSpec = tween(100, easing = EaseOutBounce)
                                        )
                                        wiggle.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(300, easing = EaseOutBounce)
                                        )
                                    }
                                }
                                AnimatedVisibility(
                                    visible = modifiable.isModified,
                                    enter = fadeIn(
                                        animationSpec = tween(300)
                                    ) + slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = tween(300),
                                    ),
                                    exit = fadeOut(
                                        animationSpec = tween(300)
                                    ) + slideOutVertically(
                                        targetOffsetY = { it },
                                        animationSpec = tween(300),
                                    ),
                                    modifier = Modifier
                                        .graphicsLayer {
                                            translationX = wiggle.value
                                        }
                                ) {
                                    val shape = RoundedCornerShape(8.dp)
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                            contentColor = MaterialTheme.colorScheme.onSurface,
                                        ),
                                        modifier = Modifier
                                            .padding(24.dp)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant,
                                                shape
                                            ),
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(16.dp, 8.dp)
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(locale["preferences.unconfirmed_changes"])
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        modifiable.reset()
                                                    },
                                                    shape = shape
                                                ) {
                                                    Text(locale["preferences.reset_changes"])
                                                }
                                                Button(
                                                    onClick = {
                                                        modifiable.apply()
                                                    },
                                                    shape = shape
                                                ) {
                                                    Text(locale["preferences.apply_changes"])
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Main function to run the preferences window standalone for testing & development.
         */
        @JvmStatic
        fun main(args: Array<String>) {
            application {
                PDEComposeWindow(
                    titleKey = "preferences",
                    fullWindowContent = true,
                    size = DpSize(850.dp, 600.dp),
                    minSize = DpSize(850.dp, 600.dp),
                ) {
                    PDETheme(darkTheme = true) {
                        preferences()
                    }
                }
                PDEComposeWindow(
                    titleKey = "preferences",
                    fullWindowContent = true,
                    size = DpSize(850.dp, 600.dp),
                    minSize = DpSize(850.dp, 600.dp),
                ) {
                    PDETheme(darkTheme = false) {
                        preferences()
                    }
                }
            }
        }
    }
}


private data class ModifiablePreference(
    val lastCloseAttempt: Long? = null,
    val isModified: Boolean,
    val apply: () -> Unit,
    val reset: () -> Unit,
)

private val LocalModifiablePreferences =
    compositionLocalOf { ModifiablePreference(null, false, { }, {}) }

/**
 * Composable function that captures an initial copy of the current preferences.
 * This allows for temporary changes to preferences that can be reset or applied later.
 *
 * @param content The composable content that will have access to the modifiable preferences.
 */
@Composable
private fun CapturePreferences(content: @Composable () -> Unit) {
    val prefs = LocalPreferences.current

    var lastCloseAttempt by remember { mutableStateOf<Long?>(null) }
    val modified = remember {
        ReactiveProperties().apply {
            prefs.entries.forEach { (key, value) ->
                setProperty(key as String, value as String)
            }
        }
    }
    val isModified = remember(
        prefs,
        // TODO: Learn how to modify preferences so listening to the object is enough
        prefs.snapshotStateMap.toMap(),
        modified,
        modified.snapshotStateMap.toMap(),
    ) {
        prefs.entries.any { (key, value) ->
            modified[key] != value
        }
    }
    if (isModified) {
        val window = LocalWindow.current
        DisposableEffect(window) {
            val operation = window.defaultCloseOperation
            window.defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
            window.rootPane.putClientProperty("Window.documentModified", true);
            val listener = object : WindowListener {
                override fun windowOpened(e: WindowEvent?) {}
                override fun windowClosing(e: WindowEvent?) {
                    lastCloseAttempt = System.currentTimeMillis()
                }

                override fun windowClosed(e: WindowEvent?) {}
                override fun windowIconified(e: WindowEvent?) {}
                override fun windowDeiconified(e: WindowEvent?) {}
                override fun windowActivated(e: WindowEvent?) {}
                override fun windowDeactivated(e: WindowEvent?) {}

            }
            window.addWindowListener(listener)
            onDispose {
                window.removeWindowListener(listener)
                window.defaultCloseOperation = operation
                window.rootPane.putClientProperty("Window.documentModified", false);
            }
        }
    }

    val apply = {
        prefs.entries.forEach { (key, value) ->
            modified.setProperty(key as String, (value ?: "") as String)
        }
    }
    val reset = {
        modified.entries.forEach { (key, value) ->
            prefs.setProperty(key as String, modified[key] ?: "")
        }
    }
    val state = ModifiablePreference(
        isModified = isModified,
        apply = apply,
        lastCloseAttempt = lastCloseAttempt,
        reset = reset
    )

    CompositionLocalProvider(
        LocalModifiablePreferences provides state
    ) {
        content()
    }
}

typealias PDEPreferencePanes = MutableMap<PDEPreferencePane, PDEPreferenceGroups>
typealias PDEPreferenceGroups = List<PDEPreferenceGroup>
typealias PDEPreferenceGroup = List<PDEPreference>
typealias PDEPreferenceControl = @Composable (preference: String?, updatePreference: (newValue: String) -> Unit) -> Unit

/**
 * Data class representing a pane of preferences.
 */
data class PDEPreferencePane(
    /**
     * The name key of this pane from the Processing locale.
     */
    val nameKey: String,
    /**
     * The icon representing this pane.
     */
    val icon: @Composable () -> Unit,
    /**
     * The pane that comes before this one in the list.
     */
    val after: PDEPreferencePane? = null,
)

/**
 * Composable function to display the contents of a preference pane.
 */
@Composable
fun PDEPreferencePane.showPane(groups: PDEPreferenceGroups) {
    Box {
        val locale = LocalLocale.current
        val state = rememberLazyListState()
        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 30.dp, end = 30.dp, bottom = 30.dp)
        ) {
            item {
                Text(
                    text = locale[nameKey],
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                )
            }
            items(groups) { group ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    ),
                ) {
                    group.forEachIndexed { index, preference ->
                        preference.showControl()
                        if (index != group.lastIndex) {
                            HorizontalDivider()
                        }
                    }

                }
            }
            item {
                val prefs = LocalPreferences.current
                TextButton(
                    onClick = {
                        groups.forEach { group ->
                            group.forEach { pref ->
                                prefs.remove(pref.key)
                            }
                        }
                    }
                ) {
                    Text(
                        text = locale["preferences.reset"],
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(12.dp)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(state)
        )
    }
}

/**
 * Data class representing a single preference in the preferences' system.
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
     * The key for the label of this preference, used for localization.
     * If null, the label will not be shown.
     */
    val labelKey: String? = null,
    /**
     * The group this preference belongs to.
     */
    val pane: PDEPreferencePane,
    /**
     * A Composable function that defines the control used to modify this preference.
     * It takes the current preference value and a function to update the preference.
     */
    val control: PDEPreferenceControl = { preference, updatePreference -> },

    /**
     * If true, no padding will be applied around this preference's UI.
     */
    val noPadding: Boolean = false,
    /**
     * If true, the title will be omitted from this preference's UI.
     */
    val noTitle: Boolean = false,
)

/**
 * Extension function to check if a list of preference groups is not empty.
 */
fun PDEPreferenceGroups?.isNotEmpty(): Boolean {
    if (this == null) return false
    for (group in this) {
        if (group.isNotEmpty()) return true
    }
    return false
}

/**
 * Composable function to display the preference's description and control.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PDEPreference.showControl() {
    val locale = LocalLocale.current
    val prefs = LocalPreferences.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!noTitle) {
            Column(
                modifier = Modifier
                    .weight(1f)

            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = locale[descriptionKey],
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (labelKey != null && locale.containsKey(labelKey)) {
                        Card {
                            Text(
                                text = locale[labelKey],
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(8.dp, 4.dp)
                            )
                        }
                    }
                }
                if (locale.containsKey("$descriptionKey.tip")) {
                    Markdown(
                        content = locale["$descriptionKey.tip"],
                        colors = markdownColor(
                            text = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        typography = markdownTypography(
                            text = MaterialTheme.typography.bodySmall,
                            paragraph = MaterialTheme.typography.bodySmall,
                            textLink = TextLinkStyles(
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                ).toSpanStyle()
                            )
                        ),
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }
        }
        val show = @Composable {
            control(prefs[key]) { newValue ->
                prefs[key] = newValue
            }
        }

        if (noPadding) {
            show()
        } else {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
            ) {
                show()
            }
        }
    }
}