plugins{
    `java-gradle-plugin`
    alias(libs.plugins.gradlePublish)

    kotlin("jvm") version libs.versions.kotlin
}

version = rootProject.version

repositories {
    mavenCentral()
}

dependencies{
    implementation(project(":java:preprocessor"))

    implementation(libs.composeGradlePlugin)
    implementation(libs.kotlinGradlePlugin)
    implementation(libs.kotlinComposePlugin)
}

gradlePlugin{
    plugins{
        create("processing"){
            id = "org.processing.java.gradle"
            implementationClass = "org.processing.java.gradle.ProcessingPlugin"
        }
    }
}
publishing{
    repositories{
        mavenLocal()
    }
}