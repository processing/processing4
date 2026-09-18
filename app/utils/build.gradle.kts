import com.vanniktech.maven.publish.SonatypeHost
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
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing{
    repositories{
        maven {
            name = "App"
            url = uri(project(":app").layout.buildDirectory.dir("resources-bundled/common/repository").get().asFile.absolutePath)
        }
    }
}
    mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    if (project.hasProperty("signing.keyId") || project.hasProperty("signingInMemoryKey"))
        signAllPublications()

    pom {
        name.set("Processing Utilities")
        description.set("Processing Utilities")
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

tasks.test {
    useJUnitPlatform()
}
