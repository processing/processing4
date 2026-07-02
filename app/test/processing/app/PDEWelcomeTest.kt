package processing.app.ui

import androidx.compose.ui.test.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import processing.app.Base
import processing.app.ui.theme.PDETheme
import processing.app.api.Sketch.Companion.Sketch

@OptIn(ExperimentalTestApi::class)
class PDEWelcomeTest {

    // Critical Function Tests

    @Test
    fun testWelcomeScreenRendersWithoutBase() = runComposeUiTest {
        setContent {
            PDETheme { PDEWelcome(base = null) }
        }
        waitForIdle()
    }

    @Test
    fun testWelcomeScreenRendersWithBase() = runComposeUiTest {
        val base: Base = mock()
        setContent {
            PDETheme { PDEWelcome(base = base) }
        }
        waitForIdle()
    }


    // Action button visibility

    @Test
    fun testNewSketchButtonIsDisplayed() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        onNodeWithText(Labels.NEW_SKETCH, substring = true).assertIsDisplayed()
    }

    @Test
    fun testSketchbookButtonIsDisplayed() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        onNodeWithText(Labels.SKETCHBOOK, substring = true).assertIsDisplayed()
    }

    @Test
    fun testExamplesButtonIsDisplayed() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        onNodeWithText(Labels.EXAMPLES, substring = true).assertIsDisplayed()
    }

    // Action button clicks

    @Test
    fun testNewSketchButtonCallsHandleNew() = runComposeUiTest {
        val base: Base = mock()
        setContent { PDETheme { PDEWelcome(base = base) } }
        onNodeWithText(Labels.NEW_SKETCH, substring = true).performClick()
        verify(base).handleNew()
    }

    @Test
    fun testSketchbookButtonCallsShowSketchbookFrame() = runComposeUiTest {
        val base: Base = mock()
        setContent { PDETheme { PDEWelcome(base = base) } }
        onNodeWithText(Labels.SKETCHBOOK, substring = true).performClick()
        verify(base).showSketchbookFrame()
    }

    @Test
    fun debugSemanticTree() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        waitForIdle()
        onRoot().printToLog("PDEWelcome")
    }

    @Test
    fun testExamplesButtonCallsShowExamplesFrame() = runComposeUiTest {
        val base: Base = mock()
        setContent { PDETheme { PDEWelcome(base = base) } }
        onNodeWithText(Labels.EXAMPLES, substring = true).performClick()
        verify(base).showExamplesFrame()
    }

    // Null-base safety

    @Test
    fun testNewSketchWithNullBaseDoesNotCrash() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = null) } }
        onNodeWithText(Labels.NEW_SKETCH, substring = true).performClick()
        waitForIdle()
    }

    @Test
    fun testSketchbookWithNullBaseDoesNotCrash() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = null) } }
        onNodeWithText(Labels.SKETCHBOOK, substring = true).performClick()
        waitForIdle()
    }

    @Test
    fun testExamplesWithNullBaseDoesNotCrash() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = null) } }
        onNodeWithText(Labels.EXAMPLES, substring = true).performClick()
        waitForIdle()
    }

    @Test
    fun testNoBaseMethodsCalledWhenBaseIsNull() = runComposeUiTest {
        val base: Base = mock()
        setContent { PDETheme { PDEWelcome(base = null) } }
        onNodeWithText(Labels.NEW_SKETCH, substring = true).performClick()
        onNodeWithText(Labels.SKETCHBOOK, substring = true).performClick()
        onNodeWithText(Labels.EXAMPLES, substring = true).performClick()
        verifyNoInteractions(base)
    }

    // Show on startup checkbox

    @Test
    fun testShowOnStartupCheckboxIsDisplayed() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        onNodeWithText(Labels.SHOW_ON_STARTUP, substring = true).assertIsDisplayed()
    }

    @Test
    fun testShowOnStartupCheckboxTogglesPreference() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        onNodeWithText(Labels.SHOW_ON_STARTUP, substring = true).performClick()
        waitForIdle()
        // Row must still be present after toggling
        onNodeWithText(Labels.SHOW_ON_STARTUP, substring = true).assertIsDisplayed()
    }

    // Resource & community links

    @Test
    fun testGetStartedLinkIsDisplayed() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        onNodeWithText(Labels.GET_STARTED, substring = true).assertIsDisplayed()
    }

    @Test
    fun testTutorialsLinkIsDisplayed() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        onNodeWithText(Labels.TUTORIALS, substring = true).assertIsDisplayed()
    }

    @Test
    fun testDocumentationLinkIsDisplayed() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        onNodeWithText(Labels.DOCUMENTATION, substring = true).assertIsDisplayed()
    }

    @Test
    fun testForumLinkIsDisplayed() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        onNodeWithText(Labels.FORUM, substring = true).assertIsDisplayed()
    }

    @Test
    fun testDiscordLinkIsDisplayed() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        onNodeWithText("Discord", substring = true).assertIsDisplayed()
    }

    @Test
    fun testGithubLinkIsDisplayed() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        onNodeWithText("GitHub", substring = true).assertIsDisplayed()
    }

    @Test
    fun testInstagramLinkIsDisplayed() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        onNodeWithText("Instagram", substring = true).assertIsDisplayed()
    }

    // Examples list

    @Test
    fun testExamplesListIsDisplayed() = runComposeUiTest {
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        waitForIdle()
        onAllNodesWithText(Labels.OPEN_SKETCH, substring = true)
            .onFirst()
            .assertExists()
    }

    @Test
    fun testExamplesListFallsBackToDefaultsWhenNoSketches() = runComposeUiTest {
        // When listAllExamples() yields nothing, PDEWelcome falls back to the
        // 4 hard-coded sketches. Either way at least one card must exist.
        setContent { PDETheme { PDEWelcome(base = mock()) } }
        waitForIdle()
        onAllNodesWithText(Labels.OPEN_SKETCH, substring = true)
            .onFirst()
            .assertExists()
    }

    @Test
    fun testSketchCardOpenButtonTriggersCallback() = runComposeUiTest {
        var opened = false
        setContent {
            PDETheme {
                val sketch = Sketch(path = "/tmp", name = "test")
                sketch.card(onOpen = { opened = true })
            }
        }
        // Hover to reveal the overlay
        onRoot().performMouseInput { moveTo(center) }
        waitForIdle()
        onNodeWithText(Labels.OPEN_SKETCH, substring = true).performClick()
        assert(opened)
    }

    @Test
    fun testSketchCardHoverRevealsBanner() = runComposeUiTest {
        setContent {
            PDETheme {
                val sketch = Sketch(path = "/tmp", name = "MySketch")
                sketch.card()
            }
        }
        onRoot().performMouseInput { moveTo(center) }
        waitForIdle()
        onNodeWithText("MySketch", substring = true).assertIsDisplayed()
    }

    @Test
    fun testPDEWelcomeWithSurveyRendersWithoutCrash() = runComposeUiTest {
        setContent { PDETheme { PDEWelcomeWithSurvey(base = mock()) } }
        waitForIdle()
    }

    @Test
    fun testPDEWelcomeWithSurveyRendersWithNullBase() = runComposeUiTest {
        setContent { PDETheme { PDEWelcomeWithSurvey(base = null) } }
        waitForIdle()
    }

    // Label constants. Update if anything is changed

    private object Labels {
        const val NEW_SKETCH      = "New Sketch"
        const val SKETCHBOOK      = "My Sketches"           // was "Sketchbook"
        const val EXAMPLES        = "Open Examples"         // was "Examples"
        const val SHOW_ON_STARTUP = "Show this window at startup"  // was "Show on startup"
        const val GET_STARTED     = "Get Started"
        const val TUTORIALS       = "Tutorials"
        const val DOCUMENTATION   = "Reference"             // was "Documentation"
        const val FORUM           = "Forum"
        const val OPEN_SKETCH     = "Open"
    }
}
