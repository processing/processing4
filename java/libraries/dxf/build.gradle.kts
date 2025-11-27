plugins{
    id("org.processing.library")
}

processing {
    library {
        version = 1
        prettyVersion = "1.0.0"

        authors = mapOf(
            "The Processing Foundation" to "https://processing.org"
        )
        url = "https://processing.org/"
        categories = listOf("file", "exporter", "dxf")

        sentence = "DXF export library for Processing"
        paragraph =
            "This library allows you to export your Processing drawings as DXF files, which can be opened in CAD applications."

    }
}

sourceSets {
    main {
        java {
            srcDirs("src")
        }
    }
}
dependencies{
    implementation("com.lowagie:itext:2.1.7")
}

/**
 * @deprecated Legacy task, use 'bundleLibrary' task provided by 'org.processing.library' plugin
 */
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
        rename { "dxf.jar" }
    }
}