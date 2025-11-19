package processing.app

import kotlin.io.path.createTempDirectory
import kotlin.test.Test

class SketchbookTest {
    @Test
    fun sketchbookTest() {
        val result = Base.getSketchbookFolder()
        assert(result != null)
    }

    @Test
    fun sketchbookIsOverridableTest() {
        val directory = createTempDirectory("scaffolding")
        val preferences = directory.resolve("preferences")
        preferences.toFile().mkdirs()
        val sketchbook = directory.resolve("sketchbook")
        sketchbook.toFile().mkdirs()

        System.setProperty("processing.sketchbook.folder", sketchbook.toAbsolutePath().toString())
        System.setProperty("processing.settings.folder", preferences.toAbsolutePath().toString())

        val result = Base.getSketchbookFolder()
        assert(result.absolutePath == sketchbook.toAbsolutePath().toString())
    }
}