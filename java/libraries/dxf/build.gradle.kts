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
    }
}