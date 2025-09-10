import com.vanniktech.maven.publish.SonatypeHost

plugins{
    java
    antlr
    alias(libs.plugins.mavenPublish)
}

repositories{
    mavenCentral()
    google()
    maven { url = uri("https://jogamp.org/deployment/maven") }
}

sourceSets{
    main{
        java{
            srcDirs("src/main/java", "../src/")
            include("processing/mode/java/preproc/**/*", "processing/app/**/*")
        }
    }
}
afterEvaluate{
    tasks.withType(Jar::class.java){
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(tasks.generateGrammarSource)
    }
}

dependencies{
    implementation(project(":app:utils"))

    implementation(libs.antlr)
    implementation(libs.eclipseJDT)

    antlr(libs.antlr4)
    implementation(libs.antlr4Runtime)
}

publishing{
    repositories{
        maven {
            name = "App"
            url = uri(project(":app").layout.buildDirectory.dir("resources-bundled/common/repository").get().asFile.absolutePath)
        }
    }
}

mavenPublishing{
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Only sign if signing is set up
    if(project.hasProperty("signing.keyId") || project.hasProperty("signing.signingInMemoryKey"))
        signAllPublications()

    pom{
        name.set("Processing Pre-processor")
        description.set("Processing Pre-processor")
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