plugins {
    java
    application
}

repositories {
    mavenCentral()
    maven { url = uri("https://jogamp.org/deployment/maven") }
}

//dependencies {
//    // Reference to Processing core
//    implementation(project(":core"))
//    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
//}
dependencies {
    implementation(project(":core"))

    // JUnit BOM to manage versions
    testImplementation(platform("org.junit:junit-bom:5.9.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    //testRuntimeOnly("org.junit.platform:test-platform-launcher:1.9.3")

    // Optional: AssertJ for better assertions
    testImplementation("org.assertj:assertj-core:3.24.2")
}


application {
    mainClass.set("ProcessingVisualTestExamples")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

//// Visual testing tasks
//tasks.register<JavaExec>("runVisualTests") {
//    description = "Run all visual tests"
//    classpath = sourceSets.main.get().runtimeClasspath
//    mainClass.set("ProcessingVisualTestExamples")
//}
//
//tasks.register<JavaExec>("runSimpleTest") {
//    description = "Verify visual testing setup"
//    classpath = sourceSets.main.get().runtimeClasspath
//    mainClass.set("SimpleTest")
//}
//
//tasks.register<JavaExec>("updateBaselines") {
//    description = "Update visual test baselines"
//    classpath = sourceSets.main.get().runtimeClasspath
//    mainClass.set("ProcessingCIHelper")
//    args("--update")
//}
//
//tasks.register<JavaExec>("runCITests") {
//    description = "Run visual tests in CI"
//    classpath = sourceSets.main.get().runtimeClasspath
//    mainClass.set("ProcessingCIHelper")
//    args("--ci")
//    systemProperty("java.awt.headless", "true")
//}
//
//tasks.register<Delete>("cleanVisualTestFiles") {
//    delete(fileTree(".") {
//        include("__screenshots__/**")
//        include("diff_*.png")
//    })
//}
//
//tasks.named("clean") {
//    dependsOn("cleanVisualTestFiles")
//}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    // Disable parallel execution to avoid Processing window conflicts
    maxParallelForks = 1

    // Add system properties
    systemProperty("java.awt.headless", "false")
}

// Task to update baselines using JUnit
tasks.register<Test>("updateBaselines") {
    description = "Update visual test baselines"
    group = "verification"

    useJUnitPlatform {
        includeTags("baseline")
    }

    systemProperty("update.baselines", "true")
    maxParallelForks = 1

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

// Task to run only visual tests (excluding slow tests)
tasks.register<Test>("visualTest") {
    description = "Run visual tests (excluding slow tests)"
    group = "verification"

    useJUnitPlatform {
        excludeTags("slow")
    }

    maxParallelForks = 1
}

// Task to run tests for specific feature
tasks.register<Test>("testShapes") {
    description = "Run shape-related visual tests"
    group = "verification"

    useJUnitPlatform {
        includeTags("shapes")
    }

    maxParallelForks = 1
}

// Legacy task - keep for backward compatibility during migration
tasks.register<JavaExec>("runSimpleTest") {
    description = "[DEPRECATED] Use 'test' instead - Verify visual testing setup"
    group = "verification"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("SimpleTest")

    doFirst {
        println("⚠️  WARNING: This task is deprecated. Please use './gradlew test' instead")
    }
}

// Legacy task - keep for backward compatibility
tasks.register<JavaExec>("runVisualTests") {
    description = "[DEPRECATED] Use 'test' instead - Run all visual tests"
    group = "verification"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("ProcessingVisualTestExamples")

    doFirst {
        println("⚠️  WARNING: This task is deprecated. Please use './gradlew test' instead")
    }
}

// CI-specific test task
tasks.register<Test>("ciTest") {
    description = "Run visual tests in CI mode"
    group = "verification"

    useJUnitPlatform()

    outputs.upToDateWhen { false }

    maxParallelForks = 1

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        //exceptionFormat = org.gradle.api.tasks.testing.TestExceptionFormat.FULL
    }

    // Generate XML reports for CI
    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }

    // Fail fast in CI
    failFast = true
}

// Clean task for visual test artifacts
tasks.register<Delete>("cleanVisualTestFiles") {
    description = "Clean visual test artifacts"
    group = "build"

    delete(fileTree(".") {
        include("diff_*.png")
    })

    // Don't delete baselines by default - be explicit
    doLast {
        println("✓ Cleaned diff images")
        println("  Baselines preserved in __screenshots__/")
        println("  To clean baselines: rm -rf __screenshots__/")
    }
}

// Separate task to clean everything including baselines (dangerous!)
tasks.register<Delete>("cleanAllVisualTestFiles") {
    description = "Clean ALL visual test files INCLUDING BASELINES (use with caution!)"
    group = "build"

    delete(fileTree(".") {
        include("__screenshots__/**")
        include("diff_*.png")
    })

    doFirst {
        println("⚠️  WARNING: This will delete all baseline images!")
    }
}

tasks.named("clean") {
    dependsOn("cleanVisualTestFiles")
}