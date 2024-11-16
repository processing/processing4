import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "org.processing.core"

repositories {
    mavenCentral()
    maven { url = uri("https://jogamp.org/deployment/maven") }
}

sourceSets{
    main{
        java{
            srcDirs("src")
        }
        resources{
            srcDirs("src")
            exclude("**/*.java")
        }
    }
}

dependencies {
    // TODO: Research on which jogl dependencies to include
    implementation("org.jogamp.gluegen:gluegen-rt:2.5.0")
    implementation("org.jogamp.jogl:jogl-all:2.5.0")

    testImplementation("junit:junit:4.13.2")
}

mavenPublishing{
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom{
        name.set("Processing Core")
        description.set("Processing Core")
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
        scm{
            url.set("https://github.com/processing/processing4-carbon-aug-19")
            connection.set("scm:git:git://github.com/processing/processing4-carbon-aug-19.git")
            developerConnection.set("scm:git:ssh://git@github.com/processing/processing4-carbon-aug-19.git")
        }
    }
}


tasks.test {
    useJUnit()
}