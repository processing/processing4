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
        create("processing"){
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