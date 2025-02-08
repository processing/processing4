package processing.app.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import processing.app.LocalPreferences
import processing.app.Messages
import processing.app.Platform
import processing.app.PlatformStart
import processing.app.watchFile
import java.io.File
import java.io.InputStream
import java.util.*

class Locale(language: String = "", val setLocale: (java.util.Locale) -> Unit) : Properties() {
    var locale: java.util.Locale = java.util.Locale.getDefault()

    init {
        fun loadResourceUTF8(path: String) {
            val stream = ClassLoader.getSystemResourceAsStream(path)
            stream?.reader(charset = Charsets.UTF_8)?.use { reader ->
                load(reader)
            }
        }
        loadResourceUTF8("PDE.properties")
        loadResourceUTF8("PDE_${locale.language}.properties")
        loadResourceUTF8("PDE_${locale.toLanguageTag()}.properties")
        loadResourceUTF8("PDE_${language}.properties")
    }

    @Deprecated("Use get instead", ReplaceWith("get(key)"))
    override fun getProperty(key: String?, default: String): String {
        val value = super.getProperty(key, default)
        if(value == default) Messages.log("Missing translation for $key")
        return value
    }
    operator fun get(key: String): String = getProperty(key, key)
    fun set(locale: java.util.Locale) {
        setLocale(locale)
    }
}
val LocalLocale = compositionLocalOf<Locale> { error("No Locale Set") }
@Composable
fun LocaleProvider(content: @Composable () -> Unit) {
    PlatformStart()

    val settingsFolder = Platform.getSettingsFolder()
    val languageFile = File(settingsFolder, "language.txt")
    watchFile(languageFile)
    var code by remember{ mutableStateOf(languageFile.readText().substring(0, 2)) }

    fun setLocale(locale: java.util.Locale) {
        java.util.Locale.setDefault(locale)
        languageFile.writeText(locale.language)
        code = locale.language
    }


    val locale = Locale(code, ::setLocale)
    Messages.log("Locale: $code")
    val dir = when(locale["locale.direction"]) {
        "rtl" -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

    CompositionLocalProvider(LocalLayoutDirection provides dir) {
        CompositionLocalProvider(LocalLocale provides locale) {
            content()
        }
    }
}