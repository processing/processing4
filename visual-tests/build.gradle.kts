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
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

application {
    mainClass.set("ProcessingVisualTestExamples")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Visual testing tasks
tasks.register<JavaExec>("runVisualTests") {
    description = "Run all visual tests"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("ProcessingVisualTestExamples")
}

tasks.register<JavaExec>("runSimpleTest") {
    description = "Verify visual testing setup"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("SimpleTest")
}

tasks.register<JavaExec>("updateBaselines") {
    description = "Update visual test baselines"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("ProcessingCIHelper")
    args("--update")
}

tasks.register<JavaExec>("runCITests") {
    description = "Run visual tests in CI"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("ProcessingCIHelper")
    args("--ci")
    systemProperty("java.awt.headless", "true")
}

tasks.register<Delete>("cleanVisualTestFiles") {
    delete(fileTree(".") {
        include("__screenshots__/**")
        include("diff_*.png")
    })
}

tasks.named("clean") {
    dependsOn("cleanVisualTestFiles")
}