package processing.app

import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.*
import java.util.Properties


const val PREFERENCES_FILE_NAME = "preferences.txt"
const val DEFAULTS_FILE_NAME = "defaults.txt"

class ReactiveProperties: Properties() {
    val _stateMap = mutableStateMapOf<String, String>()

    override fun setProperty(key: String, value: String) {
        super.setProperty(key, value)
        _stateMap[key] = value
    }

    override fun getProperty(key: String): String? {
        return _stateMap[key] ?: super.getProperty(key)
    }

    operator fun get(key: String): String? = getProperty(key)

    operator fun set(key: String, value: String) {
        setProperty(key, value)
    }
}
val LocalPreferences = compositionLocalOf<ReactiveProperties> { error("No preferences provided") }
@OptIn(FlowPreview::class)
@Composable
fun PreferencesProvider(content: @Composable () -> Unit){
    remember {
        Platform.init()
    }

    val settingsFolder = Platform.getSettingsFolder()
    val preferencesFile = settingsFolder.resolve(PREFERENCES_FILE_NAME)
    if(!preferencesFile.exists()){
        preferencesFile.createNewFile()
    }

    val update = watchFile(preferencesFile)
    val properties = remember(preferencesFile, update) { ReactiveProperties().apply {
        load((ClassLoader.getSystemResourceAsStream(DEFAULTS_FILE_NAME)?: InputStream.nullInputStream()).reader(Charsets.UTF_8))
        load(preferencesFile.inputStream().reader(Charsets.UTF_8))
    }}

    val initialState = remember(properties) { properties._stateMap.toMap() }

    LaunchedEffect(properties) {
        snapshotFlow { properties._stateMap.toMap() }
            .dropWhile { it == initialState }
            .debounce(100)
            .collect {
                preferencesFile.outputStream().use { output ->
                    output.write(
                        properties.entries
                            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.key.toString() })
                            .joinToString("\n") { (key, value) -> "$key=$value" }
                            .toByteArray()
                    )
                }
            }
    }

    CompositionLocalProvider(LocalPreferences provides properties){
        content()
    }

}
@Composable
fun watchFile(file: File): Any? {
    val scope = rememberCoroutineScope()
    var event by remember(file) {  mutableStateOf<WatchEvent<*>?> (null) }

    DisposableEffect(file){
        val fileSystem = FileSystems.getDefault()
        val watcher = fileSystem.newWatchService()
        var active = true

        val path = file.toPath()
        val parent = path.parent
        val key = parent.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
        scope.launch(Dispatchers.IO) {
            while (active) {
                for (modified in key.pollEvents()) {
                    if (modified.context() != path.fileName) continue
                    event = modified
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