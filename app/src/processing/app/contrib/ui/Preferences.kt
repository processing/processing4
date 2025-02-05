package processing.app.contrib.ui

import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import processing.app.Base
import processing.app.Platform
import java.io.File
import java.nio.file.*
import java.util.Properties


const val PREFERENCES_FILE_NAME = "preferences.txt"
const val DEFAULTS_FILE_NAME = "defaults.txt"


@Composable
fun loadPreferences(): Properties{
    Platform.init()

    val settingsFolder = Platform.getSettingsFolder()
    val preferencesFile = settingsFolder.resolve(PREFERENCES_FILE_NAME)

    if(!preferencesFile.exists()){
        preferencesFile.createNewFile()
    }
    val watched = watchFile(preferencesFile)

    val preferences by remember {
        mutableStateOf(Properties())
    }

    LaunchedEffect(watched){
        val defaults = Base::class.java.getResourceAsStream("/lib/${DEFAULTS_FILE_NAME}") ?: return@LaunchedEffect

        preferences.load(defaults)
        preferences.load(preferencesFile.inputStream())
    }

    return preferences
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