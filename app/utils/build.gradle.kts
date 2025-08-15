plugins {
    id("java")
    kotlin("jvm") version libs.versions.kotlin
}

repositories{
    mavenCentral()
    google()
    maven { url = uri("https://jogamp.org/deployment/maven") }
}

sourceSets{
    main{
        java{
            srcDirs("src")
        }
        kotlin {
            srcDirs("src")
        }
        resources {
            srcDirs("src/resources")
        }
    }
    test{
        kotlin{
            srcDirs("test")
        }
    }
}

dependencies {
    implementation(project(":core"))

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}