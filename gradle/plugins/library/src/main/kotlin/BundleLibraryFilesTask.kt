import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc

abstract class BundleLibraryFilesTask : DefaultTask() {
    @Input
    var configuration: ProcessingLibraryConfiguration? = null

    @OutputDirectory
    val outputDir = project.objects.directoryProperty()

    init {
        outputDir.convention(project.layout.buildDirectory.dir("library"))
    }

    @TaskAction
    fun bundle() {
        val configuration = configuration
            ?: throw GradleException("Processing library configuration must be provided.")
        val libraryName = configuration.name ?: project.name

        val buildDir = project.layout.buildDirectory.dir("library/$libraryName").get().asFile
        buildDir.mkdirs()

        val libDir = buildDir.resolve("library")
        libDir.mkdirs()

        // Copy the jar file
        val jarFile = project.tasks.named("jar", Jar::class.java).get().archiveFile.get().asFile
        jarFile.copyTo(libDir.resolve("$libraryName.jar"), overwrite = true)

        // Copy all runtime dependencies
        val runtimeClasspath = project.configurations.getByName("runtimeClasspath")
        runtimeClasspath.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
            val depFile = artifact.file
            depFile.copyTo(libDir.resolve(depFile.name), overwrite = true)
        }

        // Copy Examples folder
        val examplesDir = project.projectDir.resolve("examples")
        if (!examplesDir.exists() || !examplesDir.isDirectory) {
            throw GradleException("Examples folder not found in project directory.")
        }
        examplesDir.copyRecursively(buildDir.resolve("examples"), overwrite = true)

        // Copy javadoc to reference folder
        val docsDir = project.tasks.named("javadoc", Javadoc::class.java).get().destinationDir
        docsDir?.copyRecursively(buildDir.resolve("reference"), overwrite = true)

        // Create library.properties file
        val propertiesFile = buildDir.resolve("library.properties")
        propertiesFile.bufferedWriter().use { writer ->
            val properties = mapOf(
                "name" to libraryName,
                "version" to (configuration.version ?: "1.0.0"),
                "prettyVersion" to (configuration.prettyVersion ?: configuration.version ?: "1.0.0"),
                "authors" to (configuration.authors.entries.joinToString(", ") { "[${it.key}](${it.value})" }),
                "url" to configuration.url,
                "category" to configuration.categories.joinToString(", "),
                "sentence" to configuration.sentence,
                "paragraph" to configuration.paragraph,
                "minRevision" to configuration.minRevision,
                "maxRevision" to configuration.maxRevision
            )
            properties
                .filter { it.value != null && it.value.toString().isNotEmpty() }
                .forEach { (key, value) ->
                    writer.write("$key=$value\n")
                }
        }
        propertiesFile.copyTo(buildDir.resolve("../$libraryName.txt"), overwrite = true)
    }
}