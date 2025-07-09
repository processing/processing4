package processing.app.gradle

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import processing.app.Language.text
import processing.app.Mode
import processing.app.Preferences
import processing.app.Sketch
import processing.app.ui.Editor
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

    var sketch: Sketch? = null
        set (value) {
            field = value
            if(value == null) return
            // If the sketch is set, we start the build process to speed up the first run
            startJob("build")
        }

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
            sketch = sketch ?: throw IllegalStateException("Sketch is not set"),
            editor = editor
        )
        jobs.add(job)
        job.start()
    }

    private fun stopJobs(){
        jobs.forEach(GradleJob::cancel)
    }

    // Hooks for java to check if the Gradle service is running since mutableStateOf is not accessible in java
    fun getEnabled(): Boolean {
        return active.value
    }
    fun setEnabled(active: Boolean) {
        if(!active) stopJobs()
        this.active.value = active
    }
}