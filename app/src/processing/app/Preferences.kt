package processing.app

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.dropWhile
import processing.utils.Settings
import java.io.File
import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.util.*

/*
    The ReactiveProperties class extends the standard Java Properties class
    to provide reactive capabilities using Jetpack Compose's mutableStateMapOf.
    This allows UI components to automatically update when preference values change.
*/
class ReactiveProperties : Properties() {
    val snapshotStateMap = mutableStateMapOf<String, String>()

    override fun setProperty(key: String, value: String) {
        super.setProperty(key, value)
        snapshotStateMap[key] = value
    }

    override fun getProperty(key: String): String? {
        return snapshotStateMap[key] ?: super.getProperty(key)
    }

    operator fun get(key: String): String? = getProperty(key)

    operator fun set(key: String, value: String) {
        setProperty(key, value)
    }
    fun remove() {
        TODO("Not yet implemented")
    }
}

/*
    A CompositionLocal to provide access to the ReactiveProperties instance
    throughout the composable hierarchy.
 */
val LocalPreferences = compositionLocalOf<ReactiveProperties> { error("No preferences provided") }

const val PREFERENCES_FILE_NAME = "preferences.txt"
const val DEFAULTS_FILE_NAME = "defaults.txt"

/*
    This composable function sets up a preferences provider that manages application settings.
    It initializes the preferences from a file, watches for changes to that file, and saves
    any updates back to the file. It uses a ReactiveProperties class to allow for reactive
    updates in the UI when preferences change.

    usage:
    PreferencesProvider {
        // Your app content here
    }

    to access preferences:
    val preferences = LocalPreferences.current
    val someSetting = preferences["someKey"] ?: "defaultValue"
    preferences["someKey"] = "newValue"

    This will automatically save to the preferences file and update any UI components
    that are observing that key.

    to override the preferences file (for testing, etc)
        System.setProperty("processing.app.preferences.file", "/path/to/your/preferences.txt")
    to override the debounce time (in milliseconds)
        System.setProperty("processing.app.preferences.debounce", "200")

 */
@OptIn(FlowPreview::class)
@Composable
fun PreferencesProvider(content: @Composable () -> Unit) {
    val preferencesFileOverride: File? = System.getProperty("processing.app.preferences.file")?.let { File(it) }
    val preferencesDebounceOverride: Long? = System.getProperty("processing.app.preferences.debounce")?.toLongOrNull()

    val settingsFolder = Settings.getFolder()
    val preferencesFile = preferencesFileOverride ?: settingsFolder.resolve(PREFERENCES_FILE_NAME)

    if (!preferencesFile.exists()) {
        preferencesFile.mkdirs()
        preferencesFile.createNewFile()
    }

    remember {
        // check if the file has backward slashes
        if (preferencesFile.readText().contains("\\")) {
            val correctedText = preferencesFile.readText().replace("\\", "/")
            preferencesFile.writeText(correctedText)
        }
    }

    val update = watchFile(preferencesFile)


    val properties = remember(preferencesFile, update) {
        ReactiveProperties().apply {
            val defaultsStream = ClassLoader.getSystemResourceAsStream(DEFAULTS_FILE_NAME)
                ?: InputStream.nullInputStream()
            defaultsStream
                .reader(Charsets.UTF_8)
                .use { reader ->
                    load(reader)
                }
            preferencesFile
                .inputStream()
                .reader(Charsets.UTF_8)
                .use { reader ->
                    load(reader)
                }
        }
    }

    val initialState = remember(properties) { properties.snapshotStateMap.toMap() }

    // Listen for changes to the preferences and save them to file
    LaunchedEffect(properties) {
        snapshotFlow { properties.snapshotStateMap.toMap() }
            .dropWhile { it == initialState }
            .debounce(preferencesDebounceOverride ?: 100)
            .collect {

                // Save the preferences to file, sorted alphabetically
                preferencesFile.outputStream().use { output ->
                    output.write(
                        properties.entries
                            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.key.toString() })
                            .joinToString("\n") { (key, value) -> "$key=$value" }
                            .toByteArray()
                    )
                    output.close()

                    PreferencesEvents.updated()
                }
            }
    }

    CompositionLocalProvider(LocalPreferences provides properties) {
        content()
    }

}

/*
    This composable function watches a specified file for modifications. When the file is modified,
    it updates a state variable with the latest WatchEvent. This can be useful for triggering UI updates
    or other actions in response to changes in the file.

    To watch the file at the fasted speed (for testing) set the following system property:
        System.setProperty("processing.app.watchfile.forced", "true")
 */
@Composable
fun watchFile(file: File): Any? {
    val forcedWatch: Boolean = System.getProperty("processing.app.watchfile.forced").toBoolean()

    val scope = rememberCoroutineScope()
    var event by remember(file) { mutableStateOf<WatchEvent<*>?>(null) }

    DisposableEffect(file) {
        val fileSystem = FileSystems.getDefault()
        val watcher = fileSystem.newWatchService()

        var active = true

        // In forced mode we just poll the last modified time of the file
        // This is not efficient but works better for testing with temp files
        val toWatch = { file.lastModified() }
        var state = toWatch()

        val path = file.toPath()
        val parent = path.parent
        val key = parent.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
        scope.launch(Dispatchers.IO) {
            while (active) {
                if (forcedWatch) {
                    if (toWatch() == state) continue
                    state = toWatch()
                    event = object : WatchEvent<Path> {
                        override fun count(): Int = 1
                        override fun context(): Path = file.toPath().fileName
                        override fun kind(): WatchEvent.Kind<Path> = StandardWatchEventKinds.ENTRY_MODIFY
                        override fun toString(): String = "ForcedEvent(${context()})"
                    }
                    continue
                } else {
                    for (modified in key.pollEvents()) {
                        if (modified.context() != path.fileName) continue
                        event = modified
                    }
                    delay(10)
                }
            }
        }
        onDispose {
            active = false
            key.cancel()
            watcher.close()
        }
    }
    return event
}

class PreferencesEvents {
    companion object {
        val updatedListeners = mutableListOf<Runnable>()

        @JvmStatic
        fun onUpdated(callback: Runnable) {
            updatedListeners.add(callback)
        }

        fun updated() {
            updatedListeners.forEach { it.run() }
        }
    }
}