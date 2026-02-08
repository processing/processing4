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

// TODO: CI/CD for publishing the plugin to the Gradle Plugin Portal
gradlePlugin{
    plugins{
        create("processing.java"){
            id = "org.processing.java"
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