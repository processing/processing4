plugins{
    java
}

sourceSets {
    main {
        java {
            srcDirs("src")
        }
    }
}
repositories{
    mavenCentral()
    maven("https://jogamp.org/deployment/maven/")
}

dependencies{
    compileOnly(project(":core"))

    implementation("com.lowagie:itext:2.1.7")
}

tasks.register<Copy>("createLibrary"){
    dependsOn("jar")
    into(layout.buildDirectory.dir("library"))

    from(layout.projectDirectory){
        include ("library.properties")
        include("examples/**")
    }

    from(configurations.runtimeClasspath){
        into("library")
    }

    from(tasks.jar) {
        into("library")
        rename { "dxf.jar" }
    }
}