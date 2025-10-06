plugins {
    java
    application
}

repositories {
    mavenCentral()
    maven { url = uri("https://jogamp.org/deployment/maven") }
}

dependencies {
    implementation(project(":core"))

    // JUnit BOM to manage versions
    testImplementation(platform("org.junit:junit-bom:5.9.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-suite:1.9.3")

    testImplementation("org.assertj:assertj-core:3.24.2")
}


application {
    mainClass.set("ProcessingVisualTestExamples")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

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

tasks.register<Test>("testShapes") {
    description = "Run shape-related visual tests"
    group = "verification"

    useJUnitPlatform {
        includeTags("shapes")
    }

    maxParallelForks = 1
}

tasks.register<Test>("testBasicShapes") {
    description = "Run basic shapes visual tests"
    group = "verification"

    useJUnitPlatform {
        includeTags("basic")
    }

    outputs.upToDateWhen { false }
    maxParallelForks = 1

    // Add test logging to see what's happening
    testLogging {
        events("passed", "skipped", "failed", "started")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// Task to run ONLY visual tests (no other test types)
tasks.register<Test>("visualTests") {
    description = "Run all visual tests"
    group = "verification"

    useJUnitPlatform {
        // Include all tests in the visual test package
        includeEngines("junit-jupiter")
    }

    filter {
        includeTestsMatching("visual.*")
    }

    outputs.upToDateWhen { false }
    maxParallelForks = 1

    testLogging {
        events("passed", "skipped", "failed", "started")
        showStandardStreams = true
        displayGranularity = 2
    }
}

tasks.register<Test>("testRendering") {
    description = "Run rendering visual tests"
    group = "verification"

    useJUnitPlatform {
        includeTags("rendering")
    }

    outputs.upToDateWhen { false }
    maxParallelForks = 1

    testLogging {
        events("passed", "skipped", "failed", "started")
        showStandardStreams = true
    }
}

tasks.register<Test>("runSuite") {
    description = "Run specific test suite (use -PsuiteClass=SuiteName)"
    group = "verification"

    useJUnitPlatform {
        val suiteClass = project.findProperty("suiteClass") as String?
            ?: "visual.suites.AllVisualTests"
        includeTags(suiteClass)
    }

    outputs.upToDateWhen { false }
    maxParallelForks = 1
}

// Update baselines for specific suite
tasks.register<Test>("updateBaselinesForSuite") {
    description = "Update baselines for specific suite (use -Psuite=tag)"
    group = "verification"

    useJUnitPlatform {
        val suite = project.findProperty("suite") as String? ?: "baseline"
        includeTags(suite, "baseline")
    }

    systemProperty("update.baselines", "true")
    outputs.upToDateWhen { false }
    maxParallelForks = 1
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