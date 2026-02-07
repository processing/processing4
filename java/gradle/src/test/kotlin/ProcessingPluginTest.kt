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
                id("${System.getProperty("project.group")}.java")
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

    data class TemporaryProcessingLibraryResult(
        val buildResult: BuildResult,
        val libraryFolder: File
    )

    fun createTemporaryProcessingLibrary(name: String): TemporaryProcessingLibraryResult{
        val directory = TemporaryFolder()
        directory.create()
        val libraryFolder = directory.newFolder("libraries",name)
        directory.newFile("libraries/$name/build.gradle.kts").writeText("""
            plugins {
                java
            }
            tasks.jar{
                destinationDirectory.set(file("library"))
            }
        """.trimIndent())
        val srcDirectory = directory.newFolder("libraries", name,"src", "main", "java")
        directory.newFile("libraries/$name/src/main/java/Example.java").writeText("""
            package testing.example;
            
            public class Example {
                public void exampleMethod() {
                    System.out.println("Hello from Example library");
                }
            }
        """.trimIndent())
        directory.newFile("libraries/$name/settings.gradle.kts")
        directory.newFile("libraries/$name/library.properties").writeText("""
            name=$name
            author=Test Author
            version=1.0.0
            sentence=An example library
            paragraph=This is a longer description of the example library.
            category=Examples
            url=http://example.com
        """.trimIndent())

        if(isDebuggerAttached()){
            openFolderInFinder(libraryFolder)
        }

        val buildResult = GradleRunner.create()
            .withProjectDir(libraryFolder)
            .withArguments("jar")
            .withPluginClasspath()
            .withDebug(true)
            .build()


        return TemporaryProcessingLibraryResult(
            buildResult,
            libraryFolder
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
        val libraryResult = createTemporaryProcessingLibrary("ExampleLibrary")
        val (buildResult, sketchFolder, classLoader) = createTemporaryProcessingSketch("build") { sketchFolder ->
            sketchFolder.resolve("sketch.pde").writeText("""
                import testing.example.*;
                
                Example example;
                
                void setup(){
                    size(100, 100);
                    example = new Example();
                    example.exampleMethod();
                }
                
                void draw(){
                    println("Hello World");
                }
            """.trimIndent())
            sketchFolder.resolve("gradle.properties").writeText(""")
                processing.sketchbook = ${libraryResult.libraryFolder.parentFile.parentFile.absolutePath}
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
    fun testUseInternalLibraries(){

    }

    @Test
    fun testUseCodeJar(){
        // TODO: test if adding jars to the code folder works
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


