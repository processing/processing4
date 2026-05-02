import com.vanniktech.maven.publish.SonatypeHost

plugins {
    java
    alias(libs.plugins.mavenPublish)
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
        rename { "net.jar" }
    }
}

publishing {
    repositories {
        maven {
            name = "App"
            url = uri(project(":app").layout.buildDirectory.dir("resources-bundled/common/repository").get().asFile.absolutePath)
        }
    }
}

mavenPublishing {
    coordinates("$group.core", name, version.toString())
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    signAllPublications()

    pom {
        name.set("Processing Net")
        description.set("Processing Net")
        url.set("https://processing.org")
        licenses {
            license {
                name.set("LGPL")
                url.set("https://www.gnu.org/licenses/lgpl-2.1.html")
            }
        }
        developers {
            developer {
                id.set("steftervelde")
                name.set("Stef Tervelde")
            }
            developer {
                id.set("benfry")
                name.set("Ben Fry")
            }
        }
        scm {
            url.set("https://github.com/processing/processing4")
            connection.set("scm:git:git://github.com/processing/processing4.git")
            developerConnection.set("scm:git:ssh://git@github.com/processing/processing4.git")
        }
    }
}