package processing.app.gradle

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.awt.ComposePanel
import processing.app.Language.text
import processing.app.Mode
import processing.app.Preferences
import processing.app.Sketch
import processing.app.ui.Editor
import processing.app.ui.Theme
import kotlin.io.path.createTempDirectory

// TODO: Highlight errors in the editor in the right place

// TODO: ---- FUTURE ----
// TODO: Improve progress tracking and show it in the UI
// TODO: PoC new debugger/tweak mode
// TODO: Track build speed (for analytics?)
// TODO: Bundle Gradle with the app

/*
* The gradle service runs the gradle tasks and manages the gradle connection
* It will create the necessary build files for gradle to run
* Then it will kick off a new GradleJob to run the tasks
* GradleJob manages the gradle build and connects the debugger
*/
class GradleService(
    val mode: Mode,
    val editor: Editor?,
) {
    val active = mutableStateOf(Preferences.getBoolean("run.use_gradle"))
    var sketch = mutableStateOf<Sketch?>(null)
    val jobs = mutableStateListOf<GradleJob>()
    val workingDir = createTempDirectory()

    fun run(){
        startJob("run")
    }

    fun export(){
        startJob("runDistributable")
    }

    fun stop(){
        stopJobs()
    }

    private fun startJob(vararg tasks: String) {
        if(!active.value) return
        editor?.let { println(text("gradle.using_gradle"))  }

        val job = GradleJob(
            tasks = tasks,
            workingDir  = workingDir,
            sketch = sketch.value ?: throw IllegalStateException("Sketch is not set"),
            editor = editor
        )
        jobs.add(job)
        job.start()
    }

    private fun stopJobs(){
        jobs.forEach(GradleJob::cancel)
    }

    // Hooks for java to interact with the Gradle service since mutableStateOf is not accessible in java
    fun setSketch(sketch: Sketch){
        this.sketch.value = sketch
        startJob("build")
    }
    fun getEnabled(): Boolean {
        return active.value
    }
    fun setEnabled(active: Boolean) {
        if(!active) stopJobs()
        this.active.value = active
    }
}