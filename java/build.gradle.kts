plugins {
    java
}

repositories{
    mavenCentral()
    google()
    maven("https://jogamp.org/deployment/maven")
}

sourceSets{
    main{
        java{
            srcDirs("src")
            exclude("processing/mode/java/preproc/**")
        }
    }
    test{
        java{
            srcDirs("test")
        }
    }
}

dependencies{
    implementation(project(":app"))
    implementation(project(":core"))
    implementation(project(":java:preprocessor"))
    implementation(project(":app:utils"))

    implementation(libs.eclipseJDT)
    implementation(libs.eclipseJDTCompiler)
    implementation(libs.classpathExplorer)
    implementation(libs.netbeansSwing)
    implementation(libs.ant)
    implementation(libs.lsp4j)
    implementation(libs.jsoup)
    implementation(libs.antlr)

    testImplementation(libs.junit)
    testImplementation(libs.mockito)
}

tasks.compileJava{
    options.encoding = "UTF-8"
}

// LEGACY TASKS
// Most of these are shims to be compatible with the old build system
// They should be removed in the future, as we work towards making things more Gradle-native
val javaMode = { path : String -> layout.buildDirectory.dir("resources-bundled/common/modes/java/$path") }

val bundle = tasks.register<Copy>("extraResources"){
    dependsOn("copyCore")
    from(".")
    include("keywords.txt")
    include("theme/**/*")
    include("application/**/*")
    into(javaMode(""))
}
tasks.register<Copy>("copyCore"){
    val coreProject = project(":core")
    dependsOn(coreProject.tasks.jar)
    from(coreProject.tasks.jar) {
        include("core*.jar")
    }
    rename("core.+\\.jar", "core.jar")
    into(coreProject.layout.projectDirectory.dir("library"))
}

val legacyLibraries = emptyArray<String>()
legacyLibraries.forEach { library ->
    tasks.register<Copy>("library-$library-extraResources"){
        val build = project(":java:libraries:$library").tasks.named("build")
        build.configure {
            dependsOn(":java:copyCore")
        }
        dependsOn(build)
        from("libraries/$library")
        include("*.properties")
        include("library/**/*")
        include("examples/**/*")
        into( javaMode("/libraries/$library"))
        dirPermissions { unix("rwx------") };
    }
    bundle.configure {
        dependsOn("library-$library-extraResources")
    }
}

val libraries = arrayOf("dxf", "io", "net", "pdf", "serial", "svg")

libraries.forEach { library ->
    val name = "create-$library-library"
    tasks.register<Copy>(name) {
        group = "libraries"
        val project = project(":java:libraries:$library")
        val libraryTask = project.tasks.named("createLibrary")
        dependsOn(libraryTask)

        from(project.layout.buildDirectory.dir("library"))
        into(javaMode("/libraries/$library"))
    }
    bundle.configure {
        dependsOn(name)
    }
}

tasks.jar { dependsOn("extraResources") }
tasks.processResources{ finalizedBy("extraResources") }
tasks.compileTestJava{ finalizedBy("extraResources") }

tasks.test {
    useJUnit()
}