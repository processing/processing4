package processing.app

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import kotlin.test.Test

class PreferencesKtTest{
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testKeyReactivity() = runComposeUiTest {
        val newValue = (0..Int.MAX_VALUE).random().toString()
        val testKey = "test.preferences.reactivity.$newValue"
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
    }

}