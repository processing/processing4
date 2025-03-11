import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask
import org.jetbrains.compose.internal.de.undercouch.gradle.tasks.download.Download
import org.jetbrains.kotlin.fir.scopes.impl.overrides

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
val revision = rootProject.findProperty("revision") ?: "1300"

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
            Pair("processing.revision", revision),
            Pair("processing.contributions.source", "https://contributions-preview.processing.org/contribs.txt"),
            Pair("processing.download.page", "https://processing.org/download/"),
            Pair("processing.download.latest", "https://processing.org/download/latest.txt"),
            Pair("processing.tutorials", "https://processing.org/tutorials/"),
        ).map { "-D${it.first}=${it.second}" }.toTypedArray())

        nativeDistributions{
            modules("jdk.jdi", "java.compiler", "jdk.accessibility")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Processing"

            macOS{
                bundleID = "${rootProject.group}.app"
                iconFile = rootProject.file("build/macos/processing.icns")
                infoPlist{
                    extraKeysRawXml = file("macos/info.plist").readText()
                }
                entitlementsFile.set(file("macos/entitlements.plist"))
                runtimeEntitlementsFile.set(file("macos/entitlements.plist"))
                appStore = true
            }
            windows{
                iconFile = rootProject.file("build/windows/processing.ico")
                menuGroup = "Processing"
                upgradeUuid = "89d8d7fe-5602-4b12-ba10-0fe78efbd602"
            }
            linux {
                appCategory = "Programming"
                menuGroup = "Processing"
                iconFile = rootProject.file("build/linux/processing.png")
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

    implementation(compose.desktop.currentOs)

    implementation(libs.compottie)
    implementation(libs.kaml)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.junitJupiter)
    testImplementation(libs.junitJupiterParams)
}

tasks.compileJava{
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
    workingDir = layout.buildDirectory.dir("test-results").get().asFile
    workingDir.mkdirs()
}

tasks.register<Exec>("installCreateDmg") {
    onlyIf { org.gradle.internal.os.OperatingSystem.current().isMacOsX }
    commandLine("arch", "-arm64", "brew", "install", "--quiet", "create-dmg")
}
tasks.register<Exec>("packageCustomDmg"){
    onlyIf { org.gradle.internal.os.OperatingSystem.current().isMacOsX }
    group = "compose desktop"

    val distributable = tasks.named<AbstractJPackageTask>("createDistributable").get()
    dependsOn(distributable, "installCreateDmg")

    val packageName = distributable.packageName.get()
    val dir = distributable.destinationDir.get()
    val dmg = dir.file("../dmg/$packageName-$version.dmg").asFile
    val app = dir.file("$packageName.app").asFile

    dmg.parentFile.deleteRecursively()
    dmg.parentFile.mkdirs()

    val extra = mutableListOf<String>()
    val isSigned = compose.desktop.application.nativeDistributions.macOS.signing.sign.get()

    if(!isSigned) {
        val content = """
        run 'xattr -d com.apple.quarantine Processing-${version}.dmg' to remove the quarantine flag
        """.trimIndent()
        val instructions = dmg.parentFile.resolve("INSTRUCTIONS.txt")
        instructions.writeText(content)
        extra.add("--add-file")
        extra.add("INSTRUCTIONS.txt")
        extra.add(instructions.path)
        extra.add("200")
        extra.add("25")
    }

    commandLine("brew", "install", "--quiet", "create-dmg")

    commandLine("create-dmg",
        "--volname", packageName,
        "--volicon", file("macos/volume.icns"),
        "--background", file("macos/background.png"),
        "--icon", "$packageName.app", "200", "200",
        "--window-pos", "200", "200",
        "--window-size", "775", "485",
        "--app-drop-link", "500", "200",
        "--hide-extension", "$packageName.app",
        *extra.toTypedArray(),
        dmg,
        app
    )
}

tasks.register<Exec>("packageCustomMsi"){
    onlyIf { org.gradle.internal.os.OperatingSystem.current().isWindows }
    dependsOn("createDistributable")
    workingDir = file("windows")
    group = "compose desktop"

    commandLine(
        "dotnet",
        "build",
        "/p:Platform=x64",
        "/p:Version=$version",
        "/p:DefineConstants=\"Version=$version;\""
    )
}

tasks.register("generateSnapConfiguration"){
    onlyIf { org.gradle.internal.os.OperatingSystem.current().isLinux }
    val distributable = tasks.named<AbstractJPackageTask>("createDistributable").get()
    dependsOn(distributable)

    val arch = when (System.getProperty("os.arch")) {
        "amd64", "x86_64" -> "amd64"
        "aarch64" -> "arm64"
        else -> System.getProperty("os.arch")
    }

    val dir = distributable.destinationDir.get()
    val content = """
    name: ${rootProject.name}
    version: ${rootProject.version}
    base: core22
    summary: A creative coding editor
    description: |
      Processing is a flexible software sketchbook and a programming language designed for learning how to code.
    confinement: strict
    
    apps:
      processing:
        command: opt/processing/bin/Processing
        desktop: opt/processing/lib/processing-Processing.desktop
        plugs:
          - desktop
          - desktop-legacy
          - wayland
          - x11
    
    parts:
      processing:
        plugin: dump
        source: deb/processing_$version-1_$arch.deb
        source-type: deb
        stage-packages:
          - openjdk-17-jdk
        override-prime: |
          snapcraftctl prime
          chmod -R +x opt/processing/lib/app/resources/jdk-*
    """.trimIndent()
    dir.file("../snapcraft.yaml").asFile.writeText(content)
}

tasks.register<Exec>("packageSnap"){
    onlyIf { org.gradle.internal.os.OperatingSystem.current().isLinux }
    dependsOn("packageDeb", "generateSnapConfiguration")
    group = "compose desktop"

    val distributable = tasks.named<AbstractJPackageTask>("createDistributable").get()
    workingDir = distributable.destinationDir.dir("../").get().asFile

    commandLine("snapcraft")
}
tasks.register<Zip>("zipDistributable"){
    dependsOn("createDistributable")
    group = "compose desktop"

    val distributable = tasks.named<AbstractJPackageTask>("createDistributable").get()
    val dir = distributable.destinationDir.get()
    val packageName = distributable.packageName.get()

    from(dir){ eachFile{ permissions{ unix("755") } } }
    archiveBaseName.set(packageName)
    destinationDirectory.set(dir.file("../").asFile)
}

afterEvaluate{
    tasks.named("createDistributable").configure{
        finalizedBy("zipDistributable")
    }
    tasks.named("packageDmg").configure{
        dependsOn("packageCustomDmg")
        group = "compose desktop"
        actions = emptyList()
    }
    tasks.named("packageMsi").configure{
        dependsOn("packageCustomMsi")
        group = "compose desktop"
        actions = emptyList()
    }
    tasks.named("packageDistributionForCurrentOS").configure {
        dependsOn("packageSnap")
    }
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