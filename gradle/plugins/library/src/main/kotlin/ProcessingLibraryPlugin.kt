import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.toolchain.JavaLanguageVersion
import java.util.prefs.Preferences

class ProcessingLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create("processing", ProcessingLibraryExtension::class.java)
        target.plugins.apply(JavaPlugin::class.java)

        target.repositories.mavenCentral()
        target.repositories.maven { it.setUrl("https://jogamp.org/deployment/maven/") }

        // Grab processing core if available, otherwise use the published version
        val hasCore = try {
            val core = target.project(":core")
            target.dependencies.add("compileOnly", core)
            true
        } catch (_: Exception) {
            false
        }

        target.afterEvaluate {
            if (!hasCore) {
                if (extension.version == null) {
                    throw GradleException("Processing library version must be specified, please set processing.version in your build.gradle.kts")
                }
                val processingVersion = extension.version
                target.dependencies.add("compileOnly", "org.processing:core:$processingVersion")
            }
        }
        target.extensions.configure(JavaPluginExtension::class.java) { extension ->
            extension.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        }

        target.plugins.withType(JavaPlugin::class.java) {
            val jarTask = target.tasks.named("jar", Jar::class.java)
            val javaDocTask = target.tasks.named("javadoc", Javadoc::class.java)

            val bundleTask = target.tasks.register("bundleLibrary", BundleLibraryFilesTask::class.java) { task ->
                task.configuration = extension.library
                task.group = "processing"
                task.description = "Creates the Processing library folder with jar, library.properties, and examples."
                task.dependsOn(jarTask, javaDocTask)
            }

            val zipTask = target.tasks.register("zipLibrary", Zip::class.java) { task ->
                task.apply {
                    val libraryName = extension.library.name ?: target.name
                    val sourceDir = bundleTask.get().outputDir.get().asFile

                    group = "processing"
                    description = "Creates a zip & pdex archive of the Processing library folder."
                    dependsOn(bundleTask)
                    include("${libraryName}/**")

                    archiveFileName.set("$libraryName.zip")
                    from(sourceDir)
                    destinationDirectory.set(sourceDir)
                    doLast {
                        val zip = task.outputs.files.files.first()
                        zip.copyTo(sourceDir.resolve("$libraryName.pdex"), overwrite = true)
                    }
                }
            }

            target.tasks.register("installLibrary") { task ->
                task.apply {
                    group = "processing"
                    dependsOn(zipTask)
                    doLast {
                        val preferences = Preferences.userRoot().node("org/processing/app")

                        val semverRe = Regex("""^(\d+)(?:\.(\d+))?(?:\.(\d+))?(?:-([0-9A-Za-z.-]+))?""")
                        fun semverKey(v: String): Triple<Long, Boolean, String> {
                            val m = semverRe.find(v)
                            val maj = m?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
                            val min = m?.groupValues?.getOrNull(2)?.toLongOrNull() ?: 0L
                            val pat = m?.groupValues?.getOrNull(3)?.toLongOrNull() ?: 0L
                            val pre = m?.groupValues?.getOrNull(4)
                            val packed = (maj shl 40) or (min shl 20) or pat
                            return Triple(packed, pre == null, pre ?: "")
                        }

                        val installLocations = preferences.get("installLocations", "")
                            .split(",")
                            .filter { it.isNotEmpty() }
                            .mapNotNull {
                                val parts = it.split("^")
                                if (parts.size < 2) null else parts[1] to parts[0] // version to path
                            }
                            .sortedWith(Comparator { a, b ->
                                val ka = semverKey(a.first)
                                val kb = semverKey(b.first)
                                when {
                                    ka.first != kb.first -> kb.first.compareTo(ka.first)
                                    ka.second != kb.second -> kb.second.compareTo(ka.second)
                                    else -> kb.third.compareTo(ka.third)
                                }
                            })

                        val installPath = installLocations.firstOrNull()?.second
                            ?: throw GradleException("Could not find Processing install location in preferences.")

                        val libraryName = extension.library.name ?: target.name
                        val sourceDir = bundleTask.get().outputDir.get().asFile.resolve("$libraryName.pdex")

                        ProcessBuilder()
                            .command(installPath, sourceDir.absolutePath)
                            .inheritIO()
                            .start()
                    }
                }
            }

        }
    }
}