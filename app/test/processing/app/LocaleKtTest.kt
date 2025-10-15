package processing.app

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import processing.app.ui.theme.LocalLocale
import processing.app.ui.theme.LocaleProvider
import kotlin.io.path.createTempDirectory
import kotlin.test.Test

class LocaleKtTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testLocale() = runComposeUiTest {
        val tempPreferencesDir = createTempDirectory("preferences")

        System.setProperty("processing.app.preferences.folder", tempPreferencesDir.toFile().absolutePath)

        setContent {
            LocaleProvider {
                val locale = LocalLocale.current
                Text(locale["menu.file.new"], modifier = Modifier.testTag("localisedText"))

                Button(onClick = {
                    locale.setLocale(java.util.Locale("es"))
                }, modifier = Modifier.testTag("button")) {
                    Text("Change")
                }
            }
        }

        // Check if usage generates the language file if it doesn't exist
        val languageFile = tempPreferencesDir.resolve("language.txt").toFile()
        assert(languageFile.exists())

        // Check if the text is localised
        onNodeWithTag("localisedText").assertTextEquals("New")

        // Change the locale to Spanish
        onNodeWithTag("button").performClick()
        onNodeWithTag("localisedText").assertTextEquals("Nuevo")

        // Check if the preference was saved to file
        assert(languageFile.readText().substring(0, 2) == "es")
    }
}