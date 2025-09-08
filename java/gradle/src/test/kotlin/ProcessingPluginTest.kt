import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.lang.management.ManagementFactory
import java.net.URLClassLoader

class ProcessingPluginTest{
    // TODO: Test on multiple platforms since there are meaningful differences between the platforms
    data class TemporaryProcessingSketchResult(
        val buildResult: BuildResult,
        val sketchFolder: File,
        val classLoader: ClassLoader
    )

    fun createTemporaryProcessingSketch(vararg arguments: String, configure: (sketchFolder: File) -> Unit): TemporaryProcessingSketchResult{
        val directory = TemporaryFolder()
        directory.create()
        val sketchFolder = directory.newFolder("sketch")
        directory.newFile("sketch/build.gradle.kts").writeText("""
            plugins {
                id("org.processing.java")
            }
        """.trimIndent())
        directory.newFile("sketch/settings.gradle.kts")
        configure(sketchFolder)

        val buildResult = GradleRunner.create()
            .withProjectDir(sketchFolder)
            .withArguments(*arguments)
            .withPluginClasspath()
            .withDebug(true)
            .build()

        val classDir = sketchFolder.resolve("build/classes/java/main")
        val classLoader = URLClassLoader(arrayOf(classDir.toURI().toURL()), this::class.java.classLoader)

        return TemporaryProcessingSketchResult(
            buildResult,
            sketchFolder,
            classLoader
        )
    }

    @Test
    fun testSinglePDE(){
        val (buildResult, sketchFolder, classLoader) = createTemporaryProcessingSketch("build"){ sketchFolder ->
            sketchFolder.resolve("sketch.pde").writeText("""
                void setup(){
                    size(100, 100);
                }
                
                void draw(){
                    println("Hello World");
                }
            """.trimIndent())
        }

        val sketchClass = classLoader.loadClass("sketch")

        assert(sketchClass != null) {
            "Class sketch not found"
        }

        assert(sketchClass?.methods?.find { method -> method.name == "setup" } != null) {
            "Method setup not found in class sketch"
        }

        assert(sketchClass?.methods?.find { method -> method.name == "draw" } != null) {
            "Method draw not found in class sketch"
        }
    }

    @Test
    fun testMultiplePDE(){
        val (buildResult, sketchFolder, classLoader) = createTemporaryProcessingSketch("build"){ sketchFolder ->
            sketchFolder.resolve("sketch.pde").writeText("""
                void setup(){
                    size(100, 100);
                }
                
                void draw(){
                    otherFunction();
                }
            """.trimIndent())
            sketchFolder.resolve("sketch2.pde").writeText("""
                void otherFunction(){
                    println("Hi");
                }
            """.trimIndent())
        }

        val sketchClass = classLoader.loadClass("sketch")

        assert(sketchClass != null) {
            "Class sketch not found"
        }

        assert(sketchClass?.methods?.find { method -> method.name == "otherFunction" } != null) {
            "Method otherFunction not found in class sketch"
        }

    }

    @Test
    fun testJavaSourceFile(){
        val (buildResult, sketchFolder, classLoader) = createTemporaryProcessingSketch("build"){ sketchFolder ->
            sketchFolder.resolve("sketch.pde").writeText("""
                void setup(){
                    size(100, 100);
                }
                
                void draw(){
                    println("Hello World");
                }
            """.trimIndent())
            sketchFolder.resolve("extra.java").writeText("""
                class SketchJava {
                    public void javaMethod() {
                        System.out.println("Hello from Java");
                    }
                }
            """.trimIndent())
        }
        val sketchJavaClass = classLoader.loadClass("SketchJava")

        assert(sketchJavaClass != null) {
            "Class SketchJava not found"
        }

        assert(sketchJavaClass?.methods?.find { method -> method.name == "javaMethod" } != null) {
            "Method javaMethod not found in class SketchJava"
        }
    }

    @Test
    fun testWithUnsavedSource(){
        val (buildResult, sketchFolder, classLoader) = createTemporaryProcessingSketch("build"){ sketchFolder ->
            sketchFolder.resolve("sketch.pde").writeText("""
                void setup(){
                    size(100, 100);
                }
                
                void draw(){
                    println("Hello World");
                }
            """.trimIndent())
            sketchFolder.resolve("../unsaved").mkdirs()
            sketchFolder.resolve("../unsaved/sketch.pde").writeText("""
                void setup(){
                    size(100, 100);
                }
                
                void draw(){
                    println("Hello World");
                }
                
                void newMethod(){
                    println("This is an unsaved method");
                }
            """.trimIndent())
            sketchFolder.resolve("gradle.properties").writeText(""")
                processing.workingDir = ${sketchFolder.parentFile.absolutePath}
            """.trimIndent())
        }
        val sketchClass = classLoader.loadClass("sketch")

        assert(sketchClass != null) {
            "Class sketch not found"
        }

        assert(sketchClass?.methods?.find { method -> method.name == "newMethod" } != null) {
            "Method otherFunction not found in class sketch"
        }
    }

    @Test
    fun testImportingLibrary(){
        // TODO: Implement a test that imports a Processing library and uses it in the sketch
    }

    fun isDebuggerAttached(): Boolean {
        val runtimeMxBean = ManagementFactory.getRuntimeMXBean()
        val inputArguments = runtimeMxBean.inputArguments
        return inputArguments.any {
            it.contains("-agentlib:jdwp")
        }
    }
    fun openFolderInFinder(folder: File) {
        if (!folder.exists() || !folder.isDirectory) {
            println("Invalid directory: ${folder.absolutePath}")
            return
        }

        val process = ProcessBuilder("open", folder.absolutePath)
            .inheritIO()
            .start()
        process.waitFor()
    }
}


