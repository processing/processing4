import org.gradle.internal.jvm.Jvm
import org.gradle.internal.os.OperatingSystem
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask
import org.jetbrains.compose.internal.de.undercouch.gradle.tasks.download.Download
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// TODO: Update to 2.10.20 and add hot-reloading: https://github.com/JetBrains/compose-hot-reload

plugins{
    id("java")
    kotlin("jvm") version libs.versions.kotlin

    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)

    alias(libs.plugins.serialization)
    alias(libs.plugins.download)
}

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
            srcDirs("resources", listOf("fonts", "theme").map { "../build/shared/lib/$it" })
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
        mainClass = "processing.app.ProcessingKt"

        jvmArgs(*listOf(
            Pair("processing.version", rootProject.version),
            Pair("processing.revision", findProperty("revision") ?: Int.MAX_VALUE),
            Pair("processing.contributions.source", "https://contributions.processing.org/contribs"),
            Pair("processing.download.page", "https://processing.org/download/"),
            Pair("processing.download.latest", "https://processing.org/download/latest.txt"),
            Pair("processing.tutorials", "https://processing.org/tutorials/"),
        ).map { "-D${it.first}=${it.second}" }.toTypedArray())

        nativeDistributions{
            modules("jdk.jdi", "java.compiler", "jdk.accessibility", "jdk.zipfs", "java.management.rmi", "java.scripting", "jdk.httpserver")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Processing"




            fileAssociation("application/x-processing","pde", "Processing Source Code",rootProject.file("build/shared/lib/icons/pde-512.png"), rootProject.file("build/windows/pde.ico"), rootProject.file("build/macos/pde.icns"))
            fileAssociation("application/x-processing","pyde", "Processing Python Source Code",rootProject.file("build/shared/lib/icons/pde-512.png"), rootProject.file("build/windows/pde.ico"), rootProject.file("build/macos/pde.icns"))
            fileAssociation("application/x-processing","pdez", "Processing Sketch Bundle",rootProject.file("build/shared/lib/icons/pde-512.png"), rootProject.file("build/windows/pdze.ico"), rootProject.file("build/macos/pdez.icns"))
            fileAssociation("application/x-processing","pdex", "Processing Contribution Bundle", rootProject.file("build/shared/lib/icons/pde-512.png"), rootProject.file("build/windows/pdex.ico"), rootProject.file("build/macos/pdex.icns"))

            macOS{
                bundleID = "${rootProject.group}.app"
                iconFile = rootProject.file("build/macos/processing.icns")
                infoPlist{
                    extraKeysRawXml = file("macos/info.plist").readText()
                }
                entitlementsFile.set(file("macos/entitlements.plist"))
                runtimeEntitlementsFile.set(file("macos/entitlements.plist"))
                appStore = true
                jvmArgs("-Dsun.java2d.metal=true")
            }
            windows{
                iconFile = rootProject.file("build/windows/processing.ico")
                menuGroup = "Processing"
                upgradeUuid = "89d8d7fe-5602-4b12-ba10-0fe78efbd602"
            }
            linux {
                debMaintainer = "hello@processing.org"
                menuGroup = "Development;Programming;"
                appCategory = "Programming"
                iconFile = rootProject.file("build/linux/processing.png")
                // Fix fonts on some Linux distributions
                jvmArgs("-Dawt.useSystemAAFontSettings=on")

            }
        }
    }
}

dependencies {
    implementation(project(":core"))
    runtimeOnly(project(":java"))
    implementation(project(":app:utils"))

    implementation(libs.flatlaf)

    implementation(libs.jna)
    implementation(libs.jnaplatform)

    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(compose.components.uiToolingPreview)
    implementation(compose.materialIconsExtended)

    implementation(compose.desktop.currentOs)
    implementation(libs.material3)

    implementation(libs.compottie)
    implementation(libs.kaml)
    implementation(libs.markdown)
    implementation(libs.markdownJVM)

    implementation(libs.clikt)
    implementation(libs.kotlinxSerializationJson)

    @OptIn(ExperimentalComposeLibrary::class)
    testImplementation(compose.uiTest)
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

tasks.register("lsp-develop"){
    group = "processing"
    // This task is used to run the LSP server when developing the LSP server itself
    // to run the LSP server for end-users use `processing lsp` instead
    dependencies.add("runtimeOnly", project(":java"))

    // Usage: ./gradlew lsp-develop
    // Make sure the cwd is set to the project directory
    // or use -p to set the project directory

    // Modify run configuration to start the LSP server rather than the Processing IDE
    val run = tasks.named<JavaExec>("run").get()
    run.standardInput = System.`in`
    run.standardOutput = System.out
    dependsOn(run)

    // TODO: Remove after command line is integrated, then add the `lsp` argument instead, `lsp-develop` can't be removed because we still need to pipe the input and output
    run.jvmArgs("-Djava.awt.headless=true")
    compose.desktop.application.mainClass = "processing.mode.java.lsp.PdeLanguageServer"
}

val version = if(project.version == "unspecified") "1.0.0" else project.version
val distributable = { tasks.named<AbstractJPackageTask>("createDistributable").get() }
val arch = when (System.getProperty("os.arch")) {
    "amd64", "x86_64" -> "amd64"
    "aarch64" -> "arm64"
    else -> System.getProperty("os.arch")
}

tasks.register<Exec>("installCreateDmg") {
    onlyIf { OperatingSystem.current().isMacOsX }
    commandLine("arch", "-arm64", "brew", "install", "--quiet", "create-dmg")
}
tasks.register<Exec>("packageCustomDmg"){
    onlyIf { OperatingSystem.current().isMacOsX }
    group = "compose desktop"

    dependsOn(distributable(), "installCreateDmg")

    val packageName = distributable().packageName.get()
    val dir = distributable().destinationDir.get()
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

    commandLine("create-dmg",
        "--volname", packageName,
        "--volicon", file("macos/volume.icns"),
        "--background", file("macos/background.png"),
        "--icon", "$packageName.app", "190", "185",
        "--window-pos", "200", "200",
        "--window-size", "658", "422",
        "--app-drop-link", "466", "185",
        "--hide-extension", "$packageName.app",
        *extra.toTypedArray(),
        dmg,
        app
    )
}

tasks.register<Exec>("packageCustomMsi"){
    onlyIf { OperatingSystem.current().isWindows }
    dependsOn("createDistributable")
    workingDir = file("windows")
    group = "compose desktop"

    val version = if(version == "unspecified") "1.0.0" else version

    commandLine(
        "dotnet",
        "build",
        "/p:Platform=x64",
        "/p:Version=$version",
        "/p:DefineConstants=\"Version=$version;\""
    )
}

tasks.register("generateSnapConfiguration"){
    val name = findProperty("snapname") as String? ?: rootProject.name
    val confinement = (findProperty("snapconfinement") as String?).takeIf { !it.isNullOrBlank() } ?: "strict"
    val dir = distributable().destinationDir.get()
    val base = layout.projectDirectory.file("linux/snapcraft.yml")

    doFirst {
        replaceVariablesInFile(
            base,
            dir.file("../snapcraft.yaml"),
            mapOf(
                "name" to name,
                "arch" to arch,
                "version" to version as String,
                "confinement" to confinement,
                "deb" to "deb/${rootProject.name}_${version}-1_${arch}.deb"
            ),
            if (confinement == "classic") listOf("PLUGS") else emptyList()
        )
    }
}
tasks.register("generateFlatpakConfiguration"){
    val identifier = findProperty("flathubidentifier") as String? ?: "org.processing.pde"

    val dir = distributable().destinationDir.get()
    val base = layout.projectDirectory.file("linux/flathub.yml")

    doFirst {
        replaceVariablesInFile(
            base,
            dir.file("../flatpak/$identifier.yml"),
            mapOf(
                "identifier" to identifier,
                "deb" to dir.file("../deb/${rootProject.name}_${version}-1_${arch}.deb").asFile.absolutePath
            ),
            emptyList()
        )
    }
}

fun replaceVariablesInFile(
    source: RegularFile,
    target: RegularFile,
    variables: Map<String, String>,
    sections: List<String>
){
    var content = source.asFile.readText()
    for ((key, value) in variables) {
        content = content.replace("\$$key", value)
    }
    if (sections.isNotEmpty()) {
        for (section in sections) {
            val start = content.indexOf("# $section START")
            val end = content.indexOf("# $section END")
            if (start != -1 && end != -1) {
                val before = content.substring(0, start)
                val after = content.substring(end + "# $section END".length)
                content = before + after
            }
        }
    }
    target.asFile.parentFile.mkdirs()
    target.asFile.writeText(content)
}

tasks.register<Exec>("packageSnap"){
    onlyIf { OperatingSystem.current().isLinux }
    dependsOn("generateSnapConfiguration")
    group = "compose desktop"

    workingDir = distributable().destinationDir.dir("../").get().asFile
    commandLine("snapcraft")
}

tasks.register<Exec>("buildFlatpak"){
    onlyIf { OperatingSystem.current().isLinux }
    dependsOn("generateFlatpakConfiguration")
    group = "compose desktop"

    val dir = distributable().destinationDir.get()
    val identifier = findProperty("flathubidentifier") as String? ?: "org.processing.pde"

    workingDir = dir.file("../flatpak").asFile
    commandLine(
        "flatpak-builder",
        "--install-deps-from=https://flathub.org/repo/flathub.flatpakrepo",
        "--user",
        "--force-clean",
        "--repo=repo",
        "output",
        "$identifier.yml"
    )
}

tasks.register<Exec>("packageFlatpak"){
    onlyIf { OperatingSystem.current().isLinux }
    dependsOn("buildFlatpak")
    group = "compose desktop"

    val dir = distributable().destinationDir.get()
    val identifier = findProperty("flathubidentifier") as String? ?: "org.processing.pde"

    workingDir = dir.file("../flatpak").asFile
    commandLine(
        "flatpak",
        "build-bundle",
        "./repo",
        "$identifier.flatpak",
        identifier
    )
}
tasks.register<Zip>("zipDistributable"){
    dependsOn("createDistributable", "setExecutablePermissions")
    group = "compose desktop"

    val dir = distributable().destinationDir.get()
    val packageName = distributable().packageName.get()

    from(dir){ eachFile{ permissions{ unix("755") } } }
    archiveBaseName.set(packageName)
    destinationDirectory.set(dir.file("../").asFile)
}

afterEvaluate{
    // Override the default DMG task to use our custom one
    tasks.named("packageDmg").configure{
        dependsOn("packageCustomDmg")
        group = "compose desktop"
        actions = emptyList()
    }
    // Override the default MSI task to use our custom one
    tasks.named("packageMsi").configure{
        dependsOn("packageCustomMsi")
        group = "compose desktop"
        actions = emptyList()
    }
    tasks.named("packageDistributionForCurrentOS").configure {
        if(OperatingSystem.current().isMacOsX
            && compose.desktop.application.nativeDistributions.macOS.notarization.appleID.isPresent
        ){
            dependsOn("notarizeDmg")
        }
        dependsOn("zipDistributable")
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
    dirPermissions { unix("rwx------") }
}
tasks.register<Copy>("includeJdk") {
    from(Jvm.current().javaHome.absolutePath)
    destinationDir = composeResources("jdk").get().asFile

    fileTree(destinationDir).files.forEach { file ->
        file.setWritable(true, false)
        file.setReadable(true, false)
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
// TODO: Move to java mode
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
tasks.register("includeProcessingResources"){
    dependsOn(
        "includeCore",
        "includeJavaMode",
        "includeSharedAssets",
        "includeProcessingExamples",
        "includeProcessingWebsiteExamples",
        "includeJavaModeResources",
        "renameWindres"
    )
    mustRunAfter("includeJdk")
    finalizedBy("signResources")
}

tasks.register("signResources"){
    onlyIf {
        OperatingSystem.current().isMacOsX
            &&
        compose.desktop.application.nativeDistributions.macOS.signing.sign.get()
    }
    group = "compose desktop"
    val resourcesPath = composeResources("")

    // find jars in the resources directory
    val jars = mutableListOf<File>()
    doFirst{
        fileTree(resourcesPath)
            .matching { include("**/Info.plist") }
            .singleOrNull()
            ?.let { file ->
                copy {
                    from(file)
                    into(resourcesPath)
                }
            }
        fileTree(resourcesPath) {
            include("**/*.jar")
            exclude("**/*.jar.tmp/**")
        }.forEach { file ->
            val tempDir = file.parentFile.resolve("${file.name}.tmp")
            copy {
                from(zipTree(file))
                into(tempDir)
            }
            file.delete()
            jars.add(tempDir)
        }
        fileTree(resourcesPath){
            include("**/bin/**")
            include("**/*.jnilib")
            include("**/*.dylib")
            include("**/*aarch64*")
            include("**/*x86_64*")
            include("**/*ffmpeg*")
            include("**/ffmpeg*/**")
            exclude("jdk/**")
            exclude("*.jar")
            exclude("*.so")
            exclude("*.dll")
        }.forEach{ file ->
            exec {
                commandLine("codesign", "--timestamp", "--force", "--deep","--options=runtime", "--sign", "Developer ID Application", file)
            }
        }
        jars.forEach { file ->
            FileOutputStream(File(file.parentFile, file.nameWithoutExtension)).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    file.walkTopDown().forEach { fileEntry ->
                        if (fileEntry.isFile) {
                            // Calculate the relative path for the zip entry
                            val zipEntryPath = fileEntry.relativeTo(file).path
                            val entry = ZipEntry(zipEntryPath)
                            zos.putNextEntry(entry)

                            // Copy file contents to the zip
                            fileEntry.inputStream().use { input ->
                                input.copyTo(zos)
                            }
                            zos.closeEntry()
                        }
                    }
                }
            }

            file.deleteRecursively()
        }
        file(composeResources("Info.plist")).delete()
    }


}
tasks.register("setExecutablePermissions") {
    description = "Sets executable permissions on binaries in Processing.app resources"
    group = "compose desktop"

    doLast {
        val resourcesPath = layout.buildDirectory.dir("compose/binaries")
        fileTree(resourcesPath) {
            include("**/resources/**/bin/**")
            include("**/resources/**/lib/**")
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

afterEvaluate {
    tasks.named("prepareAppResources").configure {
        dependsOn("includeProcessingResources")
    }
    tasks.named("createDistributable").configure {
        dependsOn("includeJdk")
        finalizedBy("setExecutablePermissions")
    }
}
