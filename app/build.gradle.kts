import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.internal.de.undercouch.gradle.tasks.download.Download

// TODO: Update to 2.10.20 and add hot-reloading: https://github.com/JetBrains/compose-hot-reload

plugins{
    id("java")
    kotlin("jvm") version libs.versions.kotlin

    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.serialization)
    alias(libs.plugins.download)
}

group = rootProject.group
version = rootProject.version

repositories{
    mavenCentral()
    google()
    maven { url = uri("https://jogamp.org/deployment/maven") }
}

sourceSets{
    main{
        java{
            srcDirs("src")
        }
        kotlin{
            srcDirs("src")
        }
        resources{
            srcDirs("resources", listOf("languages", "fonts", "theme").map { "../build/shared/lib/$it" })
        }
    }
    test{
        kotlin{
            srcDirs("test")
        }
    }
}

compose.desktop {
    application {
        mainClass = "processing.app.ui.Start"

        jvmArgs(*listOf(
            Pair("processing.version", version),
            Pair("processing.revision", "1300"),
            Pair("processing.contributions.source", "https://contributions-preview.processing.org/contribs.txt"),
            Pair("processing.download.page", "https://processing.org/download/"),
            Pair("processing.download.latest", "https://processing.org/download/latest.txt"),
            Pair("processing.tutorials", "https://processing.org/tutorials/"),
        ).map { "-D${it.first}=${it.second}" }.toTypedArray())

        nativeDistributions{
            modules("jdk.jdi", "java.compiler", "jdk.accessibility", "jdk.zipfs")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Processing"

            macOS{
                bundleID = "org.processing.app"
                iconFile = project.file("../build/macos/processing.icns")
                infoPlist{
                    extraKeysRawXml = layout.projectDirectory.file("info.plist").asFile.readText()
                }
                entitlementsFile.set(project.file("entitlements.plist"))
                runtimeEntitlementsFile.set(project.file("entitlements.plist"))
            }
            windows{
                iconFile = project.file("../build/windows/processing.ico")
                menuGroup = "Processing"
                upgradeUuid = "89d8d7fe-5602-4b12-ba10-0fe78efbd602"
            }
            linux {
                appCategory = "Programming"
                menuGroup = "Processing"
                iconFile = project.file("../build/linux/processing.png")
                // Fix fonts on some Linux distributions
                jvmArgs("-Dawt.useSystemAAFontSettings=on")

                fileAssociation("pde", "Processing Source Code", "application/x-processing")
                fileAssociation("pyde", "Processing Python Source Code", "application/x-processing")
                fileAssociation("pdez", "Processing Sketch Bundle", "application/x-processing")
                fileAssociation("pdex", "Processing Contribution Bundle", "application/x-processing")
            }
        }
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.flatlaf)

    implementation(libs.jna)
    implementation(libs.jnaplatform)

    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(compose.components.uiToolingPreview)
    implementation(compose.materialIconsExtended)

    implementation(compose.desktop.currentOs)

    implementation(libs.compottie)
    implementation(libs.kaml)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.junitJupiter)
    testImplementation(libs.junitJupiterParams)
}

tasks.test {
    useJUnitPlatform()
    workingDir = file("build/test")
    workingDir.mkdirs()
}

tasks.compileJava{
    options.encoding = "UTF-8"
}


// LEGACY TASKS
// Most of these are shims to be compatible with the old build system
// They should be removed in the future, as we work towards making things more Gradle-native
val composeResources = { subPath: String -> layout.buildDirectory.dir("resources-bundled/common/$subPath") }
compose.desktop.application.nativeDistributions.appResourcesRootDir.set(composeResources("../"))

tasks.register<Copy>("includeCore"){
    val core = project(":core")
    dependsOn(core.tasks.jar)
    from(core.layout.buildDirectory.dir("libs"))
    from(core.configurations.runtimeClasspath)
    into(composeResources("core/library"))
}
tasks.register<Copy>("includeJavaMode") {
    val java = project(":java")
    dependsOn(java.tasks.jar)
    from(java.layout.buildDirectory.dir("libs"))
    from(java.configurations.runtimeClasspath)
    into(composeResources("modes/java/mode"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.register<Download>("includeJdk") {
    val os = DefaultNativePlatform.getCurrentOperatingSystem()
    val arch = when (System.getProperty("os.arch")) {
        "amd64", "x86_64" -> "x64"
        else -> System.getProperty("os.arch")
    }
    val platform = when {
        os.isWindows -> "windows"
        os.isMacOsX -> "mac"
        else -> "linux"
    }

    val javaVersion = System.getProperty("java.version").split(".")[0]
    val imageType = "jdk"

    src("https://api.adoptium.net/v3/binary/latest/" +
            "$javaVersion/ga/" +
            "$platform/" +
            "$arch/" +
            "$imageType/" +
            "hotspot/normal/eclipse?project=jdk")

    val extension = if (os.isWindows) "zip" else "tar.gz"
    val jdk = layout.buildDirectory.file("tmp/jdk-$platform-$arch.$extension")
    dest(jdk)
    overwrite(false)
    doLast {
        copy {
            val archive = if (os.isWindows) { zipTree(jdk) } else { tarTree(jdk) }
            from(archive){ eachFile{ permissions{ unix("755") } } }
            into(composeResources(""))
        }
    }
}
tasks.register<Copy>("includeSharedAssets"){
    from("../build/shared/")
    into(composeResources(""))
}
tasks.register<Download>("includeProcessingExamples") {
    val examples = layout.buildDirectory.file("tmp/processing-examples.zip")
    src("https://github.com/processing/processing-examples/archive/refs/heads/main.zip")
    dest(examples)
    overwrite(false)
    doLast{
        copy{
            from(zipTree(examples)){ // remove top level directory
                exclude("processing-examples-main/README.md")
                exclude("processing-examples-main/.github/**")
                eachFile { relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray()) }
                includeEmptyDirs = false
            }
            into(composeResources("/modes/java/examples"))
        }
    }
}
tasks.register<Download>("includeProcessingWebsiteExamples") {
    val examples = layout.buildDirectory.file("tmp/processing-website.zip")
    src("https://github.com/processing/processing-website/archive/refs/heads/main.zip")
    dest(examples)
    overwrite(false)
    doLast{
        copy{
            from(zipTree(examples)){
                include("processing-website-main/content/examples/**")
                eachFile { relativePath = RelativePath(true, *relativePath.segments.drop(3).toTypedArray()) }
                includeEmptyDirs = false
                exclude { it.name.contains(".es.") || it.name == "liveSketch.js" }
            }
            into(composeResources("modes/java/examples"))
        }
    }
}
tasks.register<Copy>("includeJavaModeResources") {
    val java = project(":java")
    dependsOn(java.tasks.named("extraResources"))
    from(java.layout.buildDirectory.dir("resources-bundled"))
    into(composeResources("../"))
}
tasks.register<Copy>("renameWindres") {
    dependsOn("includeSharedAssets","includeJavaModeResources")
    val dir = composeResources("modes/java/application/launch4j/bin/")
    val os = DefaultNativePlatform.getCurrentOperatingSystem()
    val platform = when {
        os.isWindows -> "windows"
        os.isMacOsX -> "macos"
        else -> "linux"
    }
    from(dir) {
        include("*-$platform*")
        rename("(.*)-$platform(.*)", "$1$2")
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(dir)
}
afterEvaluate {
    tasks.named("prepareAppResources").configure {
        dependsOn(
            "includeCore",
            "includeJavaMode",
            "includeJdk",
            "includeSharedAssets",
            "includeProcessingExamples",
            "includeProcessingWebsiteExamples",
            "includeJavaModeResources",
            "renameWindres"
        )
    }
    tasks.register("setExecutablePermissions") {
        description = "Sets executable permissions on binaries in Processing.app resources"
        group = "compose desktop"

        doLast {
            val resourcesPath = layout.buildDirectory.dir("compose/binaries")
            fileTree(resourcesPath) {
                include("**/resources/**/bin/**")
                include("**/resources/**/*.sh")
                include("**/resources/**/*.dylib")
                include("**/resources/**/*.so")
                include("**/resources/**/*.exe")
            }.forEach { file ->
                if (file.isFile) {
                    file.setExecutable(true, false)
                }
            }
        }
    }
    tasks.findByName("createDistributable")?.finalizedBy("setExecutablePermissions")
}