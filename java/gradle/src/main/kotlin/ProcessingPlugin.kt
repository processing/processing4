package org.processing.java.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.tasks.TaskDependencyFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.net.Socket
import javax.inject.Inject

class ProcessingPlugin @Inject constructor(private val objectFactory: ObjectFactory) : Plugin<Project> {
    override fun apply(project: Project) {
        val sketchName = project.layout.projectDirectory.asFile.name.replace(Regex("[^a-zA-Z0-9_]"), "_")

        val isProcessing = project.findProperty("processing.version") != null
        val processingVersion = project.findProperty("processing.version") as String?
            ?: javaClass.classLoader.getResourceAsStream("version.properties")?.use { stream ->
                java.util.Properties().apply { load(stream) }.getProperty("version")
            }?.takeIf { it != "unspecified" } ?: "4.5.5"
        val processingGroup = project.findProperty("processing.group") as String? ?: "org.processing"
        val workingDir = project.findProperty("processing.workingDir") as String?
        val debugPort = project.findProperty("processing.debugPort") as String?
        val logPort = project.findProperty("processing.logPort") as String?
        val errPort = project.findProperty("processing.errPort") as String?

        // TODO: Setup sketchbook when using as a standalone plugin, use the Java Preferences
        val sketchbook = project.findProperty("processing.sketchbook") as String?
        val settings = project.findProperty("processing.settings") as String?
        val root = project.findProperty("processing.root") as String?

        val webgpu = (project.findProperty("processing.webgpu") as String?)?.toBoolean() ?: false
        val javaVersion = if (webgpu) 25 else 17

        // Apply the Java plugin to the Project, equivalent of
        // plugins {
        //     java
        // }
        project.plugins.apply(JavaPlugin::class.java)

        project.extensions.configure(JavaPluginExtension::class.java) { ext ->
            ext.toolchain { spec ->
                spec.languageVersion.set(JavaLanguageVersion.of(javaVersion))
            }
        }
        project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
            task.compilerOptions.jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        }

        if(isProcessing){
            // Set the build directory to a temp file so it doesn't clutter up the sketch folder
            // Only if the build directory doesn't exist, otherwise proceed as normal
            if(!project.layout.buildDirectory.asFile.get().exists()) {
                project.layout.buildDirectory.set(File(project.findProperty("processing.workingDir") as String))
            }
            // Disable the wrapper in the sketch to keep it cleaner
            project.tasks.findByName("wrapper")?.enabled = false
        }

        // Add kotlin support, equivalent of
        // plugins {
        //     kotlin("jvm") version "1.8.0"
        //     kotlin("plugin.compose") version "1.8.0"
        // }
        project.plugins.apply("org.jetbrains.kotlin.jvm")
        // Add jetpack compose support
        project.plugins.apply("org.jetbrains.kotlin.plugin.compose")
        // Add the compose plugin to wrap the sketch in an executable
        project.plugins.apply("org.jetbrains.compose")

        // Add the Processing core library (within Processing from the internal maven repo and outside from the internet), equivalent of
        // dependencies {
        //     implementation("org.processing:core:4.3.4")
        // }
        project.dependencies.add("implementation", "$processingGroup:core:${processingVersion}")

        // Add the jars in the code folder, equivalent of
        // dependencies {
        //     implementation(fileTree("src") { include("**/code/*.jar") })
        // }
        project.dependencies.add("implementation", project.fileTree("src").apply { include("**/code/*.jar") })

        // Add the repositories necessary for building the sketch, equivalent of
        // repositories {
        //     maven("https://jogamp.org/deployment/maven")
        //     mavenCentral()
        //     mavenLocal()
        // }
        project.repositories.add(project.repositories.maven { it.setUrl("https://jogamp.org/deployment/maven") })
        project.repositories.add(project.repositories.mavenCentral())
        project.repositories.add(project.repositories.mavenLocal())

        // Configure the compose Plugin, equivalent of
        // compose {
        //     application {
        //         mainClass.set(sketchName)
        //         nativeDistributions {
        //             includeAllModules()
        //         }
        //     }
        // }
        project.extensions.configure(ComposeExtension::class.java) { extension ->
            extension.extensions.getByType(DesktopExtension::class.java).application { application ->
                // Set the class to be executed initially
                application.mainClass = sketchName
                application.nativeDistributions.includeAllModules = true
                application.jvmArgs("--enable-native-access=ALL-UNNAMED")
                if(debugPort != null) {
                    application.jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$debugPort")
                }
            }
        }

        // TODO: Add support for customizing distributables
        // TODO: Setup sensible defaults for the distributables

        // Add convenience tasks for running, presenting, and exporting the sketch outside of Processing
        if(!isProcessing) {
            project.tasks.create("sketch").apply {
                group = "processing"
                description = "Runs the Processing sketch"
                dependsOn("run")
            }
            project.tasks.create("present").apply {
                group = "processing"
                description = "Presents the Processing sketch"
                doFirst {
                    project.tasks.withType(JavaExec::class.java).configureEach { task ->
                        task.systemProperty("processing.fullscreen", "true")
                    }
                }
                finalizedBy("run")
            }
            project.tasks.create("export").apply {
                group = "processing"
                description = "Creates a distributable version of the Processing sketch"

                dependsOn("createDistributable")

            }
        }

        val javaToolchains = project.extensions.getByType(org.gradle.jvm.toolchain.JavaToolchainService::class.java)
        val launcher = javaToolchains.launcherFor { it.languageVersion.set(JavaLanguageVersion.of(javaVersion)) }

        project.afterEvaluate {
            // Copy the result of create distributable to the project directory
            project.tasks.named("createDistributable") { task ->
                task.doLast {
                    project.copy {
                        it.from(project.tasks.named("createDistributable").get().outputs.files)
                        it.into(project.layout.projectDirectory)
                    }
                }
            }
            project.tasks.withType(JavaExec::class.java).configureEach { task ->
                task.executable(launcher.get().executablePath.asFile.absolutePath)
                task.jvmArgs("--enable-native-access=ALL-UNNAMED")
                if (System.getProperty("os.name").lowercase().contains("mac")) {
                    task.jvmArgs("-XstartOnFirstThread")
                }
            }
        }

        // Move the processing variables into javaexec tasks so they can be used in the sketch as well
        project.tasks.withType(JavaExec::class.java).configureEach { task ->
            project.properties
                .filterKeys { it.startsWith("processing") }
                .forEach { (key, value) -> task.systemProperty(key, value) }

            task.jvmArgs("--enable-native-access=ALL-UNNAMED")
            if (System.getProperty("os.name").lowercase().contains("mac")) {
                task.jvmArgs("-XstartOnFirstThread")
            }

            // Connect the stdio to the PDE if ports are specified
            if(logPort != null) task.standardOutput =  Socket("localhost", logPort.toInt()).outputStream
            if(errPort != null) task.errorOutput = Socket("localhost", errPort.toInt()).outputStream

        }

        // For every Java Source Set (main, test, etc) add a PDE source set that includes .pde files
        // and a task to process them before compilation
        project.extensions.getByType(JavaPluginExtension::class.java).sourceSets.first().let{ sourceSet ->
            val pdeSourceSet = objectFactory.newInstance(
                DefaultPDESourceDirectorySet::class.java,
                objectFactory.sourceDirectorySet("${sourceSet.name}.pde", "${sourceSet.name} Processing Source")
            )

            // Configure the PDE source set to include all .pde files in the sketch folder except those in the build directory
            pdeSourceSet.apply {
                srcDir("./")
                srcDir("$workingDir/unsaved")

                filter.include("**/*.pde")
                filter.exclude("${project.layout.buildDirectory.asFile.get().name}/**")
            }
            sourceSet.allSource.source(pdeSourceSet)

            // Add top level java source files
            sourceSet.java.srcDir(project.layout.projectDirectory).apply {
                include("/*.java")
            }

            // Create a task to process the .pde files before compiling the java sources
            val pdeTaskName = sourceSet.getTaskName("preprocess", "PDE")
            val pdeTask = project.tasks.register(pdeTaskName, PDETask::class.java) { task ->
                task.description = "Processes the ${sourceSet.name} PDE"
                task.source = pdeSourceSet
                task.sketchName = sketchName

                // Set the output of the pre-processor as the input for the java compiler
                sourceSet.java.srcDir(task.outputDirectory)
            }

            // Resolve sketch+library deps at config time. Adding deps from a
            // TaskAction fails once a downstream task has already resolved
            // runtimeClasspath (e.g. Compose's `run`).
            addLegacyDependencies(project, pdeSourceSet.srcDirs,
                listOfNotNull(sketchbook?.let { File(it, "libraries") },
                              root?.let { File(it).resolve("modes/java/libraries") }))

            // Make sure that the PDE tasks runs before the java compilation task
            project.tasks.named(sourceSet.compileJavaTaskName) { task ->
                task.dependsOn(pdeTaskName)
            }
        }
    }
    private fun addLegacyDependencies(project: Project, sketchDirs: Set<File>, libraryRoots: List<File>) {
        project.dependencies.add("runtimeOnly", "org.jogamp.jogl:jogl-all-main:2.6.0")
        project.dependencies.add("runtimeOnly", "org.jogamp.gluegen:gluegen-rt:2.6.0")

        val os = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").lowercase()
        val variant = when {
            os.contains("mac") -> "macosx-universal"
            os.contains("win") && arch.contains("64") -> "windows-amd64"
            os.contains("linux") && arch.contains("aarch64") -> "linux-aarch64"
            os.contains("linux") && arch.contains("arm") -> "linux-arm"
            os.contains("linux") && arch.contains("amd64") -> "linux-amd64"
            else -> null
        }
        if (variant != null) {
            project.dependencies.add("runtimeOnly", "org.jogamp.gluegen:gluegen-rt:2.6.0:natives-$variant")
            project.dependencies.add("runtimeOnly", "org.jogamp.jogl:nativewindow:2.6.0:natives-$variant")
            project.dependencies.add("runtimeOnly", "org.jogamp.jogl:newt:2.6.0:natives-$variant")
        }

        // Reduce each import to its package prefix: `a.b.*` and `a.b.C` both become `a.b`,
        // mirroring how the PDE preprocessor normalizes import statements
        val imports = sketchDirs
            .flatMap { dir -> dir.walkTopDown().filter { it.extension == "pde" }.toList() }
            .flatMap { Regex("""^\s*import\s+(?:static\s+)?([\w.]+(?:\.\*)?)\s*;""", RegexOption.MULTILINE).findAll(it.readText()).map { m -> m.groupValues[1] } }
            .map { it.removeSuffix(".*") }
            .toSet()
        if (imports.isEmpty()) return

        val libraryJars = libraryRoots
            .filter { it.exists() }
            .flatMap { it.listFiles { f -> f.isDirectory }?.toList() ?: emptyList() }
            .mapNotNull { folder -> folder.resolve("library").takeIf { it.isDirectory } }
            .flatMap { it.listFiles { f -> f.extension == "jar" }?.toList() ?: emptyList() }

        val matched = mutableSetOf<File>()
        imports.forEach { import ->
            libraryJars.forEach { jar ->
                java.util.jar.JarFile(jar).use { jf ->
                    val hit = jf.entries().asSequence()
                        .filter { it.name.endsWith(".class") }
                        .map { it.name.substringBeforeLast('/').replace('/', '.') }
                        .any { pkg -> pkg == import || pkg.startsWith("$import.") || import.startsWith("$pkg.") }
                    if (hit) matched.add(jar)
                }
            }
        }
        if (matched.isNotEmpty()) {
            project.dependencies.add("implementation", project.files(matched))
        }
    }

    abstract class DefaultPDESourceDirectorySet @Inject constructor(
        sourceDirectorySet: SourceDirectorySet,
        taskDependencyFactory: TaskDependencyFactory
    ) : DefaultSourceDirectorySet(sourceDirectorySet, taskDependencyFactory), SourceDirectorySet
}

