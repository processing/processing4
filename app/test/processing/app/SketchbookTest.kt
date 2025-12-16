package processing.app

import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

class SketchbookTest {
    @Test
    fun sketchbookTest() {
        val result = Base.getSketchbookFolder()
        assert(result != null)
    }

    @Test
    fun sketchbookIsOverridableTest() {
        val directory = createTempDirectory("scaffolding")
        val sketchbook = directory.resolve("sketchbook")
        sketchbook.toFile().mkdirs()
        val sketchbookAbs = sketchbook.toAbsolutePath().toString()
        System.setProperty("processing.sketchbook.folder", sketchbookAbs)

        val result = Base.getSketchbookFolder()
        assertEquals(sketchbookAbs, result.absolutePath)
    }
}