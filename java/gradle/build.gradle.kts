plugins{
    `java-gradle-plugin`
    alias(libs.plugins.gradlePublish)

    kotlin("jvm") version libs.versions.kotlin
}

version = rootProject.version

repositories {
    mavenCentral()
    maven { url = uri("https://jogamp.org/deployment/maven") }
}

dependencies{
    implementation(project(":java:preprocessor"))

    implementation("org.jetbrains.compose:compose-gradle-plugin:1.7.3")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
}

gradlePlugin{
    plugins{
        create("processing"){
            id = "org.processing"
            implementationClass = "org.processing.gradle.ProcessingPlugin"
        }
    }
}
publishing{
    repositories{
        mavenLocal()
    }
}