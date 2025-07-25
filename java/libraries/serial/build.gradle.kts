plugins {
    java
}

sourceSets {
    main {
        java {
            srcDirs("src")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":core"))
    // TODO: https://github.com/java-native/jssc
    implementation(files("library/jssc.jar"))
}

tasks.register<Copy>("createLibrary") {
    dependsOn("jar")
    into(layout.buildDirectory.dir("library"))
    from(layout.projectDirectory) {
        include("library.properties")
        include("examples/**")
    }
    from(configurations.runtimeClasspath) {
        into("library")
    }
    from(tasks.jar) {
        into("library")
    }
}