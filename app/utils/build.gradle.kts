plugins {
    id("java")
    alias(libs.plugins.mavenPublish)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

publishing{
    repositories{
        maven {
            name = "App"
            url = uri(project(":app").layout.buildDirectory.dir("resources-bundled/common/repository").get().asFile.absolutePath)
        }
    }
}

tasks.test {
    useJUnitPlatform()
}