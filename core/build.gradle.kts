import com.vanniktech.maven.publish.SonatypeHost
import processing.gradle.*

plugins {
    id("java")
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.mavenPublish)
}

repositories {
    mavenCentral()
    maven { url = uri("https://jogamp.org/deployment/maven") }
}

val enableWebGPU = findProperty("enableWebGPU")?.toString()?.toBoolean() ?: true

sourceSets{
    main{
        java{
            srcDirs("src")
            if (!enableWebGPU) {
                exclude("processing/webgpu/**")
                exclude("processing/ffi/**")
            }
        }
        resources{
            srcDirs("src")
            exclude("**/*.java")
        }
    }
    test{
        java{
            srcDirs("test")
        }
    }
}

dependencies {
    implementation(libs.jogl)
    implementation(libs.gluegen)

    testImplementation(libs.junit)
}

if (enableWebGPU) {
    val currentPlatform = PlatformUtils.detect()
    val libProcessingDir = file("${project.rootDir}/libProcessing")
    val rustTargetDir = file("$libProcessingDir/target")
    val nativeOutputDir = file("${layout.buildDirectory.get()}/native/${currentPlatform.target}")

    val buildRustRelease by tasks.registering(CargoBuildTask::class) {
        cargoWorkspaceDir.set(libProcessingDir)
        manifestPath.set("ffi/Cargo.toml")
        release.set(true)
        cargoPath.set(PlatformUtils.getCargoPath())
        outputLibrary.set(file("$rustTargetDir/release/${currentPlatform.libName}"))

        inputs.files(fileTree("$libProcessingDir/ffi/src"))
        inputs.file("$libProcessingDir/ffi/Cargo.toml")
        inputs.file("$libProcessingDir/ffi/build.rs")
        inputs.file("$libProcessingDir/ffi/cbindgen.toml")
        inputs.files(fileTree("$libProcessingDir/renderer/src"))
        inputs.file("$libProcessingDir/renderer/Cargo.toml")
        inputs.file("$libProcessingDir/Cargo.toml")
        outputs.file("$libProcessingDir/ffi/include/processing.h")
    }

    val copyNativeLibs by tasks.registering(Copy::class) {
        group = "rust"
        description = "Copy processing library to build directory"

        dependsOn(buildRustRelease)

        from("$rustTargetDir/release") {
            include(currentPlatform.libName)
        }

        into(nativeOutputDir)
    }

    val bundleNativeLibs by tasks.registering(Copy::class) {
        group = "rust"
        description = "Bundle native library into resources"

        dependsOn(copyNativeLibs)

        from(nativeOutputDir)
        into("${sourceSets.main.get().output.resourcesDir}/native/${currentPlatform.target}")
    }

    val cleanRust by tasks.registering(CargoCleanTask::class) {
        cargoWorkspaceDir.set(libProcessingDir)
        manifestPath.set("ffi/Cargo.toml")
        cargoPath.set(PlatformUtils.getCargoPath())

        mustRunAfter(buildRustRelease)
    }

    tasks.named("clean") {
        dependsOn(cleanRust)
    }

    val generatedJavaDir = file("${layout.buildDirectory.get()}/generated/sources/jextract/java")

    sourceSets.main {
        java.srcDirs(generatedJavaDir)
    }

    val jextractVersionString = "22-jextract+6-47"
    val jextractDirectory = file("${gradle.gradleUserHomeDir}/jextract-22")
    val jextractTarballFile = file("${gradle.gradleUserHomeDir}/jextract-$jextractVersionString.tar.gz")

    val downloadJextract by tasks.registering(DownloadJextractTask::class) {
        jextractVersion.set(jextractVersionString)
        platform.set(currentPlatform.jextractPlatform)
        jextractDir.set(jextractDirectory)
        downloadTarball.set(jextractTarballFile)

        onlyIf { !jextractDirectory.exists() }
    }

    val makeJextractExecutable by tasks.registering(Exec::class) {
        group = "rust"
        description = "Make jextract binary executable on Unix systems"

        dependsOn(downloadJextract)
        onlyIf { !System.getProperty("os.name").lowercase().contains("windows") }

        val jextractBin = file("$jextractDirectory/bin/jextract")
        commandLine("chmod", "+x", jextractBin.absolutePath)
    }

    val generateJavaBindings by tasks.registering(GenerateJextractBindingsTask::class) {
        dependsOn(buildRustRelease)

        val userJextract = JextractUtils.findUserJextract()
        if (userJextract == null) {
            dependsOn(downloadJextract, makeJextractExecutable)
        }

        headerFile.set(file("$libProcessingDir/ffi/include/processing.h"))
        outputDirectory.set(generatedJavaDir)
        targetPackage.set("processing.ffi")

        jextractPath.set(userJextract ?: "$jextractDirectory/bin/${JextractUtils.getExecutableName()}")
    }

    tasks.named("compileJava") {
        dependsOn(generateJavaBindings)
    }

    tasks.named("compileKotlin") {
        dependsOn(generateJavaBindings)
    }

    tasks.named("processResources") {
        dependsOn(bundleNativeLibs)
    }
}

mavenPublishing{
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom{
        name.set("Processing Core")
        description.set("Processing Core")
        url.set("https://processing.org")
        licenses {
            license {
                name.set("LGPL")
                url.set("https://www.gnu.org/licenses/lgpl-2.1.html")
            }
        }
        developers {
            developer {
                id.set("steftervelde")
                name.set("Stef Tervelde")
            }
            developer {
                id.set("benfry")
                name.set("Ben Fry")
            }
        }
        scm{
            url.set("https://github.com/processing/processing4")
            connection.set("scm:git:git://github.com/processing/processing4.git")
            developerConnection.set("scm:git:ssh://git@github.com/processing/processing4.git")
        }
    }
}


tasks.test {
    useJUnit()
}
tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.compileJava{
    options.encoding = "UTF-8"
}
