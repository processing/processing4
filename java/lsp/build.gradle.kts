import com.vanniktech.maven.publish.SonatypeHost

plugins{
    java
    application

    alias(libs.plugins.mavenPublish)
}

application{
    mainClass = "processing.mode.java.lsp.PdeLanguageServer"
    applicationDefaultJvmArgs = listOf("-Djna.nosys=true","-Djava.awt.headless=true")
}

repositories{
    mavenCentral()
    google()
    maven { url = uri("https://jogamp.org/deployment/maven") }
}

sourceSets{
    main{
        java{
            srcDirs("../src/")
            include("processing/mode/java/lsp/**/*")
        }
    }
}

dependencies{
    implementation(project(":core"))
    implementation(project(":app"))
    implementation(project(":java"))
    implementation(project(":java:preprocessor"))

    implementation(libs.lsp4j)
    implementation(libs.jsoup)
    implementation(libs.eclipseJDT)
}

mavenPublishing{
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom{
        name.set("Processing Language Server")
        description.set("Processing Language Server")
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
            url.set("https://github.com/processing/processing4")
            connection.set("scm:git:git://github.com/processing/processing4.git")
            developerConnection.set("scm:git:ssh://git@github.com/processing/processing4.git")
        }
    }
}
tasks.installDist {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}