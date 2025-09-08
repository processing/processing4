package processing.app.gradle

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.ui.awt.ComposePanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    var sketch = mutableStateOf<Sketch?>(null, neverEqualPolicy())
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
            workingDir = workingDir,
            sketch = sketch.value ?: throw IllegalStateException("Sketch is not set"),
            editor = editor
        )
        jobs.add(job)
        job.start()
    }

    private fun stopJobs(){
        jobs.forEach(GradleJob::cancel)
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    /*
    Watch the sketch folder for changes and start a build job when the sketch is modified
    This need to be done properly to use hooks in the future but right now this is the simplest way to do it
     */
    init{
        scope.launch {
            var path = ""
            var modified = false
            var sketched: Sketch? = null
            while(true){
                sketch.value?.let { sketch ->
                    if(sketch.folder.absolutePath != path){
                        path = sketch.folder.absolutePath
                        if(sketched == sketch){
                            // The same sketch has its folder changed, trigger updates downstream from the service
                            this@GradleService.sketch.value = sketch
                        }else {
                            sketched = sketch
                        }
                        startJob("build")
                    }
                    if(sketch.isModified != modified){
                        modified = sketch.isModified
                        if(!modified){
                            // If the sketch is no longer modified, start the build job, aka build on save
                            startJob("build")
                        }
                    }
                }


                delay(100)
            }
        }
    }

    // Hooks for java to interact with the Gradle service since mutableStateOf is not accessible in java
    fun setSketch(sketch: Sketch){
        this.sketch.value = sketch
    }
    fun getEnabled(): Boolean {
        return active.value
    }
    fun setEnabled(active: Boolean) {
        if(!active) stopJobs()
        this.active.value = active
    }
}