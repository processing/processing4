package processing.app

import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentCaptor
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test


class SchemaTest {
    private val base: Base = mock<Base>{

    }
    companion object {
        val preferences: MockedStatic<Preferences> = mockStatic(Preferences::class.java)
    }


    @Test
    fun testLocalFiles() {
        val file = "/this/is/a/local/file"
        Schema.handleSchema("pde://$file", base)
        verify(base).handleOpen(file)
    }

    @Test
    fun testNewSketch() {
        Schema.handleSchema("pde://sketch/new", base)
        verify(base).handleNew()
    }


    @Test
    fun testCustomBase64Sketch(){
        Schema.handleSchema("pde://sketch/base64/LyoqCiAqIEFycmF5IE9iamVjdHMuIAogKiAKICogRGVtb25zdHJhdGVzIHRoZSBzeW50YXggZm9yIGNyZWF0aW5nIGFuIGFycmF5IG9mIGN1c3RvbSBvYmplY3RzLiAKICovCgppbnQgdW5pdCA9IDQwOwppbnQgY291bnQ7Ck1vZHVsZVtdIG1vZHM7Cgp2b2lkIHNldHVwKCkgewogIHNpemUoNjQwLCAzNjApOwogIG5vU3Ryb2tlKCk7CiAgaW50IHdpZGVDb3VudCA9IHdpZHRoIC8gdW5pdDsKICBpbnQgaGlnaENvdW50ID0gaGVpZ2h0IC8gdW5pdDsKICBjb3VudCA9IHdpZGVDb3VudCAqIGhpZ2hDb3VudDsKICBtb2RzID0gbmV3IE1vZHVsZVtjb3VudF07CgogIGludCBpbmRleCA9IDA7CiAgZm9yIChpbnQgeSA9IDA7IHkgPCBoaWdoQ291bnQ7IHkrKykgewogICAgZm9yIChpbnQgeCA9IDA7IHggPCB3aWRlQ291bnQ7IHgrKykgewogICAgICBtb2RzW2luZGV4KytdID0gbmV3IE1vZHVsZSh4KnVuaXQsIHkqdW5pdCwgdW5pdC8yLCB1bml0LzIsIHJhbmRvbSgwLjA1LCAwLjgpLCB1bml0KTsKICAgIH0KICB9Cn0KCnZvaWQgZHJhdygpIHsKICBiYWNrZ3JvdW5kKDApOwogIGZvciAoTW9kdWxlIG1vZCA6IG1vZHMpIHsKICAgIG1vZC51cGRhdGUoKTsKICAgIG1vZC5kaXNwbGF5KCk7CiAgfQp9?pde=Module:Y2xhc3MgTW9kdWxlIHsKICBpbnQgeE9mZnNldDsKICBpbnQgeU9mZnNldDsKICBmbG9hdCB4LCB5OwogIGludCB1bml0OwogIGludCB4RGlyZWN0aW9uID0gMTsKICBpbnQgeURpcmVjdGlvbiA9IDE7CiAgZmxvYXQgc3BlZWQ7IAogIAogIC8vIENvbnRydWN0b3IKICBNb2R1bGUoaW50IHhPZmZzZXRUZW1wLCBpbnQgeU9mZnNldFRlbXAsIGludCB4VGVtcCwgaW50IHlUZW1wLCBmbG9hdCBzcGVlZFRlbXAsIGludCB0ZW1wVW5pdCkgewogICAgeE9mZnNldCA9IHhPZmZzZXRUZW1wOwogICAgeU9mZnNldCA9IHlPZmZzZXRUZW1wOwogICAgeCA9IHhUZW1wOwogICAgeSA9IHlUZW1wOwogICAgc3BlZWQgPSBzcGVlZFRlbXA7CiAgICB1bml0ID0gdGVtcFVuaXQ7CiAgfQogIAogIC8vIEN1c3RvbSBtZXRob2QgZm9yIHVwZGF0aW5nIHRoZSB2YXJpYWJsZXMKICB2b2lkIHVwZGF0ZSgpIHsKICAgIHggPSB4ICsgKHNwZWVkICogeERpcmVjdGlvbik7CiAgICBpZiAoeCA+PSB1bml0IHx8IHggPD0gMCkgewogICAgICB4RGlyZWN0aW9uICo9IC0xOwogICAgICB4ID0geCArICgxICogeERpcmVjdGlvbik7CiAgICAgIHkgPSB5ICsgKDEgKiB5RGlyZWN0aW9uKTsKICAgIH0KICAgIGlmICh5ID49IHVuaXQgfHwgeSA8PSAwKSB7CiAgICAgIHlEaXJlY3Rpb24gKj0gLTE7CiAgICAgIHkgPSB5ICsgKDEgKiB5RGlyZWN0aW9uKTsKICAgIH0KICB9CiAgCiAgLy8gQ3VzdG9tIG1ldGhvZCBmb3IgZHJhd2luZyB0aGUgb2JqZWN0CiAgdm9pZCBkaXNwbGF5KCkgewogICAgZmlsbCgyNTUpOwogICAgZWxsaXBzZSh4T2Zmc2V0ICsgeCwgeU9mZnNldCArIHksIDYsIDYpOwogIH0KfQAA", base)
        val captor = ArgumentCaptor.forClass(String::class.java)

        verify(base).handleOpenUntitled(captor.capture())

        val file = File(captor.value)
        assert(file.exists())

        val extra = file.parentFile.resolve("Module.pde")
        assert(extra.exists())
        file.parentFile.deleteRecursively()
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun testBase64SketchAndExtraFiles() {
        val sketch = """
        void setup(){
        
        }
        void draw(){
        
        }
        """.trimIndent()

        val base64 = Base64.encode(sketch.toByteArray())
        Schema.handleSchema("pde://sketch/base64/$base64?pde=AnotherFile:$base64", base)

        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(base).handleOpenUntitled(captor.capture())

        val file = File(captor.value)
        assert(file.exists())
        assert(file.readText() == sketch)

        val extra = file.parentFile.resolve("AnotherFile.pde")
        assert(extra.exists())
        assert(extra.readText() == sketch)
        file.parentFile.deleteRecursively()
    }

    @Test
    fun testURLSketch() {
        Schema.handleSchema("pde://sketch/url/github.com/processing/processing-examples/raw/refs/heads/main/Basics/Arrays/Array/Array.pde", base)
        waitForSchemeJobsToComplete()

        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(base).handleOpenUntitled(captor.capture())
        val output = File(captor.value)
        assert(output.exists())
        assert(output.name == "Array.pde")
        assert(output.extension == "pde")
        assert(output.parentFile.name == "Array")

        output.parentFile.parentFile.deleteRecursively()
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "Module.pde:https://github.com/processing/processing-examples/raw/refs/heads/main/Basics/Arrays/ArrayObjects/Module.pde",
        "Module.pde",
        "Module:Module.pde",
        "Module:https://github.com/processing/processing-examples/raw/refs/heads/main/Basics/Arrays/ArrayObjects/Module.pde",
        "Module.pde:github.com/processing/processing-examples/raw/refs/heads/main/Basics/Arrays/ArrayObjects/Module.pde"
    ])
    fun testURLSketchWithFile(file: String){
        Schema.handleSchema("pde://sketch/url/github.com/processing/processing-examples/raw/refs/heads/main/Basics/Arrays/ArrayObjects/ArrayObjects.pde?pde=$file", base)
        waitForSchemeJobsToComplete()

        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(base).handleOpenUntitled(captor.capture())

        // wait for threads to resolve
        Thread.sleep(1000)

        val output = File(captor.value)
        assert(output.parentFile.name == "ArrayObjects")
        assert(output.exists())
        assert(output.parentFile.resolve("Module.pde").exists())
        output.parentFile.parentFile.deleteRecursively()
    }

    @Test
    fun testPreferences() {
        Schema.handleSchema("pde://preferences?test=value", base)
        preferences.verify {
            Preferences.set("test", "value")
            Preferences.save()
        }
    }

    fun waitForSchemeJobsToComplete(){
        runBlocking {
            joinAll(*Schema.jobs.toTypedArray())
        }
    }
}