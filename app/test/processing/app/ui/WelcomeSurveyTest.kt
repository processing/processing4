package processing.app.ui

import androidx.compose.ui.test.*
import processing.app.ui.theme.PDETheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class WelcomeSurveyTest {

    @Test
    fun `survey card title is displayed`() = runDesktopComposeUiTest {
        setContent {
            PDETheme {
                SurveyInvitation()
            }
        }

        onNodeWithText("Take the Community Survey").assertIsDisplayed()
    }

    @Test
    fun `survey card description is displayed`() = runDesktopComposeUiTest {
        setContent {
            PDETheme {
                SurveyInvitation()
            }
        }

        onNodeWithText("Processing is free, open-source, and shaped by its community. Your answers help us focus on what matters most.").assertIsDisplayed()
    }

    @Test
    fun `survey card is clickable`() = runDesktopComposeUiTest {
        setContent {
            PDETheme {
                SurveyInvitation()
            }
        }

        onNodeWithText("Take the Community Survey").performClick()
    }
}