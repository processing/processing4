package processing.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Language
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import processing.app.Platform
import processing.app.ui.theme.LocalLocale
import processing.app.ui.theme.PDEChip
import processing.app.watchFile
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.inputStream

data class Language(
    val name: String,
    val code: String,
    val locale: Locale,
    val properties: Properties
)

var jarFs: FileSystem? = null

@Composable
fun LanguageChip(){
    var expanded by remember { mutableStateOf(false) }

    val settingsFolder = Platform.getSettingsFolder()
    val languageFile = File(settingsFolder, "language.txt")
    watchFile(languageFile)

    val main = ClassLoader.getSystemResource("PDE.properties")?: return

    val languages = remember {
        val list = when(main.protocol){
            "file" -> {
                val path = Paths.get(main.toURI())
                Files.list(path.parent)
            }
            "jar" -> {
                val uri = main.toURI()
                jarFs = jarFs ?: FileSystems.newFileSystem(uri, emptyMap<String, Any>()) ?: return@remember null
                Files.list(jarFs!!.getPath("/"))
            }
            else -> null
        } ?: return@remember null

        list.toList()
            .map { Pair(it, it.fileName.toString()) }
            .filter { (_, fileName) -> fileName.startsWith("PDE_") && fileName.endsWith(".properties") }
            .map { (path, _) ->
                path.inputStream().reader(Charsets.UTF_8).use {
                    val properties = Properties()
                    properties.load(it)

                    val code = path.fileName.toString().removeSuffix(".properties").replace("PDE_", "")
                    val locale = Locale.forLanguageTag(code)
                    val name = locale.getDisplayName(locale)

                    return@map Language(
                        name,
                        code,
                        locale,
                        properties
                    )
                }
            }
            .sortedBy { it.name.lowercase() }
    } ?: return

    val current = languageFile.readText(Charsets.UTF_8).substring(0, 2)
    val currentLanguage = remember(current) { languages.find { it.code.startsWith(current) } ?: languages.first()}

    val locale = LocalLocale.current

    PDEChip(onClick = { expanded = !expanded }, leadingIcon = {
        Image(
            imageVector = Icons.Outlined.Language,
            contentDescription = "Language",
            colorFilter = ColorFilter.tint(color = LocalContentColor.current),
            modifier = Modifier
                .padding(start = 8.dp)
                .size(typography.body1.fontSize.value.dp)
        )
    }) {
        Text(currentLanguage.name)
        Image(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = locale["welcome.action.tutorials"],
            colorFilter = ColorFilter.tint(color = LocalContentColor.current),
            modifier = Modifier
                .size(typography.body1.fontSize.value.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ){
            for (language in languages){
                DropdownMenuItem(onClick = {
                    locale.set(language.locale)
                    expanded = false
                }) {
                    Text(language.name)
                }
            }
        }
    }
}