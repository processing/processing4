plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation("org.apache.xmlgraphics:batik-all:1.19")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
sourceSets {
    main {
        java.srcDirs("src")
    }
}

tasks.jar {
    archiveBaseName.set("svg")
    destinationDirectory.set(file("library"))
}

tasks.clean {
    delete("bin", "library/svg.jar")
}