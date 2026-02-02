package processing.app.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import processing.app.Messages
import processing.app.watchFile
import processing.utils.Settings
import java.io.File
import java.util.*

/**
 * The Locale class extends the standard Java Properties class
 * to provide localization capabilities.
 * It loads localization resources from property files based on the specified language code.
 * The class also provides a method to change the current locale and update the application accordingly.
 * Usage:
 * ```
 * val locale = Locale("es") { newLocale ->
 *     // Handle locale change, e.g., update UI or restart application
 * }
 * val localizedString = locale["someKey"]
 * ```
 */
class Locale(language: String = "", val setLocale: ((java.util.Locale) -> Unit)? = null) : Properties() {
    var locale: java.util.Locale = java.util.Locale.getDefault()

    fun loadResource(resourcePath: String) {
        val stream = ClassLoader.getSystemResourceAsStream(resourcePath) ?: return
        load(stream.reader(Charsets.UTF_8))
    }

    init {
        loadResource("languages/PDE.properties")
        loadResource("languages/PDE_${locale.language}.properties")
        loadResource("languages/PDE_${locale.toLanguageTag()}.properties")
        if (language.isNotEmpty()) {
            loadResource("languages/PDE_${language}.properties")
        }
    }

    @Deprecated("Use get instead", ReplaceWith("get(key)"))
    override fun getProperty(key: String?, default: String): String {
        val value = super.getProperty(key, default)
        if (value == default) Messages.log("Missing translation for $key")
        return value
    }

    operator fun get(key: String): String = getProperty(key, key)
    fun set(locale: java.util.Locale) {
        setLocale?.invoke(locale)
    }
}

/**
 * A CompositionLocal to provide access to the Locale instance
 *     throughout the composable hierarchy. see [LocaleProvider]
 * Usage:
 * ```
 * val locale = LocalLocale.current
 * val localizedString = locale["someKey"]
 * ```
 */
val LocalLocale = compositionLocalOf<Locale> { error("No Locale Set") }
var LastLocaleUpdate by mutableStateOf(0L)

/**
 * This composable function sets up a locale provider that manages application localization.
 * It initializes the locale from a language file, watches for changes to that file, and updates
 * the locale accordingly. It uses a [Locale] class to handle loading of localized resources.
 *
 * Usage:
 * ```
 * LocaleProvider {
 *     // Your app content here
 * }
 * ```
 *
 * To access the locale:
 * ```
 * val locale = LocalLocale.current
 * val localizedString = locale["someKey"]
 * ```
 *
 * To change the locale:
 * ```
 * locale.set(java.util.Locale("es"))
 * ```
 * This will update the `language.txt` file and reload the locale.
 */
@Composable
fun LocaleProvider(content: @Composable () -> Unit) {
    val settingsFolder = Settings.getFolder()
    val languageFile = File(settingsFolder, "language.txt")
    watchFile(languageFile)

    remember(languageFile) {
        if (languageFile.exists()) return@remember
        Messages.log("Creating language file at ${languageFile.absolutePath}")
        settingsFolder.mkdirs()
        languageFile.writeText(java.util.Locale.getDefault().language)
    }

    val update = watchFile(languageFile)
    var code by remember(languageFile, update, LastLocaleUpdate) {
        mutableStateOf(
            languageFile.readText().substring(0, 2)
        )
    }
    remember(code) {
        val locale = java.util.Locale(code)
        java.util.Locale.setDefault(locale)
    }

    fun setLocale(locale: java.util.Locale) {
        Messages.log("Setting locale to ${locale.language}")
        languageFile.writeText(locale.language)
        code = locale.language
        LastLocaleUpdate = System.currentTimeMillis()
    }


    val locale = Locale(code, ::setLocale)
    remember(code) { Messages.log("Loaded Locale: $code") }
    val dir = when (locale["locale.direction"]) {
        "rtl" -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

    CompositionLocalProvider(LocalLayoutDirection provides dir) {
        CompositionLocalProvider(LocalLocale provides locale) {
            content()
        }
    }
}