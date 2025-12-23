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

    implementation("org.apache.xmlgraphics:batik-all:1.19")
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
        rename { "svg.jar" }
    }
}

