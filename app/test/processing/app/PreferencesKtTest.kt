package processing.app

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import java.util.Properties
import kotlin.io.path.createFile
import kotlin.io.path.createTempDirectory
import kotlin.test.Test

class PreferencesKtTest{
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testKeyReactivity() = runComposeUiTest {
        val directory = createTempDirectory("preferences")
        val tempPreferences = directory
            .resolve("preferences.txt")
            .createFile()
            .toFile()

        // Set system properties for testing
        System.setProperty("processing.app.preferences.file", tempPreferences.absolutePath)
        System.setProperty("processing.app.preferences.debounce", "0")
        System.setProperty("processing.app.watchfile.forced", "true")

        val newValue = (0..Int.MAX_VALUE).random().toString()
        val testKey = "test.preferences.reactivity"

        setContent {
            PreferencesProvider {
                val preferences = LocalPreferences.current
                Text(preferences[testKey] ?: "default", modifier = Modifier.testTag("text"))

                Button(onClick = {
                    preferences[testKey] = newValue
                }, modifier = Modifier.testTag("button")) {
                    Text("Change")
                }
            }
        }

        onNodeWithTag("text").assertTextEquals("default")
        onNodeWithTag("button").performClick()
        onNodeWithTag("text").assertTextEquals(newValue)

        val preferences = Properties()
        preferences.load(tempPreferences.inputStream().reader(Charsets.UTF_8))

        // Check if the preference was saved to file
        assert(preferences[testKey] == newValue)


        val nextValue = (0..Int.MAX_VALUE).random().toString()
        // Overwrite the file to see if the UI updates
        tempPreferences.writeText("$testKey=${nextValue}")

        onNodeWithTag("text").assertTextEquals(nextValue)
    }
}