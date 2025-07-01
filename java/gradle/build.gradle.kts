plugins{
    `java-gradle-plugin`
    alias(libs.plugins.gradlePublish)

    kotlin("jvm") version libs.versions.kotlin
}

repositories {
    mavenCentral()
}

dependencies{
    implementation(project(":java:preprocessor"))

    implementation(libs.composeGradlePlugin)
    implementation(libs.kotlinGradlePlugin)
    implementation(libs.kotlinComposePlugin)

    testImplementation(libs.junit)
}

gradlePlugin{
    plugins{
        create("processing"){
            id = "org.processing.gradle"
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