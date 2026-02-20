plugins{
    `java-gradle-plugin`
    alias(libs.plugins.gradlePublish)

    kotlin("jvm") version libs.versions.kotlin
}

repositories {
    mavenCentral()
    maven("https://jogamp.org/deployment/maven")
}

dependencies{
    implementation(project(":java:preprocessor"))

    implementation(libs.composeGradlePlugin)
    implementation(libs.kotlinGradlePlugin)
    implementation(libs.kotlinComposePlugin)

    testImplementation(project(":core"))
    testImplementation(libs.junit)
}

gradlePlugin{
    website = "https://processing.org/"
    vcsUrl = "https://github.com/processing/processing4"
    plugins{
        create("processing.java"){
            id = "$group.java"
            displayName = "Processing Plugin"
            description = "Gradle plugin for building Processing sketches"
            tags = listOf("processing", "sketch", "dsl")
            implementationClass = "org.processing.java.gradle.ProcessingPlugin"
        }
    }
}

publishing{
    repositories{
        mavenLocal()
        maven {
            name = "App"
            url = uri(project(":app").layout.buildDirectory.dir("resources-bundled/common/repository").get().asFile.absolutePath)
        }
    }
}
// Grab the group before running tests, since the group is used in the test configuration and may be modified by the publishing configuration
val testGroup = group.toString()
tasks.withType<Test>().configureEach {
    systemProperty("project.group", testGroup)
}

tasks.register("writeVersion") {
    // make the version available to the plugin at runtime by writing it to a properties file in the resources directory
    doLast {
        val file = layout.buildDirectory.file("resources/main/version.properties").get().asFile
        file.parentFile.mkdirs()
        file.writeText("version=${project.version}")
    }
}

tasks.named("processResources") {
    dependsOn("writeVersion")
}