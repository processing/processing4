import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java")
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.mavenPublish)
}

repositories {
    mavenCentral()
    maven { url = uri("https://jogamp.org/deployment/maven") }
}

sourceSets{
    main{
        java{
            srcDirs("src")
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

val osName = System.getProperty("os.name").lowercase()
val osArch = System.getProperty("os.arch").lowercase()

val platformName = when {
    osName.contains("mac") || osName.contains("darwin") -> "macos"
    osName.contains("win") -> "windows"
    osName.contains("linux") -> "linux"
    else -> throw GradleException("Unsupported OS: $osName")
}

val archName = when {
    osArch.contains("aarch64") || osArch.contains("arm") -> "aarch64"
    osArch.contains("x86_64") || osArch.contains("amd64") -> "x86_64"
    else -> throw GradleException("Unsupported architecture: $osArch")
}

val libExtension = when (platformName) {
    "macos" -> "dylib"
    "windows" -> "dll"
    "linux" -> "so"
    else -> throw GradleException("Unknown platform: $platformName")
}

val libName = when (platformName) {
    "windows" -> "processing.$libExtension"
    else -> "libprocessing.$libExtension"
}

val platformTarget = "$platformName-$archName"
val libProcessingDir = file("${project.rootDir}/libProcessing")
val rustTargetDir = file("$libProcessingDir/target")
val nativeOutputDir = file("${layout.buildDirectory.get()}/native/$platformTarget")

val cargoPath = System.getenv("CARGO_HOME")?.let { "$it/bin/cargo" }
    ?: "${System.getProperty("user.home")}/.cargo/bin/cargo"

val buildRustRelease by tasks.registering(Exec::class) {
    group = "rust"
    description = "Build Rust FFI library in release mode for current platform"

    workingDir = libProcessingDir
    commandLine = listOf(cargoPath, "build", "--release", "--manifest-path", "ffi/Cargo.toml")

    inputs.files(fileTree("$libProcessingDir/ffi/src"))
    inputs.file("$libProcessingDir/ffi/Cargo.toml")
    inputs.file("$libProcessingDir/Cargo.toml")
    inputs.file("$libProcessingDir/ffi/build.rs")
    inputs.file("$libProcessingDir/ffi/cbindgen.toml")

    outputs.file("$rustTargetDir/release/$libName")
    outputs.file("$libProcessingDir/ffi/include/processing.h")

    doFirst {
        logger.lifecycle("Building Rust library for $platformTarget...")
    }
}

val copyNativeLibs by tasks.registering(Copy::class) {
    group = "rust"
    description = "Copy processing library to build directory"

    dependsOn(buildRustRelease)

    from("$rustTargetDir/release") {
        include("libprocessing.a")
        include("libprocessing.$libExtension")
    }

    into(nativeOutputDir)

    doFirst {
        logger.lifecycle("Copying native libraries to $nativeOutputDir")
    }
}

val bundleNativeLibs by tasks.registering(Copy::class) {
    group = "rust"
    description = "Bundle native library into resources"

    dependsOn(copyNativeLibs)

    from(nativeOutputDir)
    into("${sourceSets.main.get().output.resourcesDir}/native/$platformTarget")

    doFirst {
        logger.lifecycle("Bundling libraries for $platformTarget into resources")
    }
}

val jextractPath = "${System.getProperty("user.home")}/jextract-22/bin/jextract"
val generatedJavaDir = file("${layout.buildDirectory.get()}/generated/sources/jextract/java")
val headerFile = file("$libProcessingDir/ffi/include/processing.h")

sourceSets.main {
    java.srcDirs(generatedJavaDir)
}

val generateJavaBindings by tasks.registering(Exec::class) {
    group = "rust"
    description = "Generate Java bindings from libProcessing headers using jextract"

    dependsOn(buildRustRelease)

    inputs.file(headerFile)

    outputs.dir(generatedJavaDir)

    doFirst {
        generatedJavaDir.mkdirs()
        logger.lifecycle("Generating Java bindings from $headerFile...")
    }

    commandLine = listOf(
        jextractPath,
        "--output", generatedJavaDir.absolutePath,
        "--target-package", "processing.ffi",
        headerFile.absolutePath
    )
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
