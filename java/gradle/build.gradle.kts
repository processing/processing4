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

tasks.withType<Test>().configureEach {
    systemProperty("project.group", group ?: "org.processing")
}