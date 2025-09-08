plugins {
    `java-gradle-plugin`
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.gradlePublish)

}


repositories {
    mavenCentral()
}

dependencies{
    implementation("org.jetbrains.compose.hot-reload:hot-reload-gradle-plugin:1.0.0-beta03")
}

gradlePlugin{
    plugins{
        create("processing.java.hotreload"){
            id = "org.processing.java.hotreload"
            implementationClass = "org.processing.java.gradle.ProcessingHotReloadPlugin"
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