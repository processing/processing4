plugins {
    kotlin("jvm") version libs.versions.kotlin apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.jetbrainsCompose) apply false

    id("com.github.node-gradle.node") version "7.1.0"
    id("java")
}

// Set the build directory to not /build to prevent accidental deletion through the clean action
// Can be deleted after the migration to Gradle is complete
layout.buildDirectory = file(".build")

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1") // For JSON parsing
}

configure<com.github.gradle.node.NodeExtension> {
    version.set("20.10.0")
    npmVersion.set("10.2.3")
    download.set(true)
    workDir.set(file("${project.projectDir}/.build/nodejs"))
    npmWorkDir.set(file("${project.projectDir}/.build/npm"))
}

//tasks.register("testNodeSetup") {
//    description = "Test that Node.js plugin is working"
//    dependsOn("nodeSetup") // This task is provided by the plugin
//    doLast {
//        println("Node.js plugin setup completed successfully!")
//    }
//}
//
//// Create a test package.json and run npm commands
//tasks.register("createTestPackage") {
//    description = "Create a test package.json for Processing"
//    doLast {
//        val packageJson = file("package.json")
//        if (!packageJson.exists()) {
//            packageJson.writeText("""
//                {
//                  "name": "processing4-test",
//                  "version": "1.0.0",
//                  "description": "Node.js integration test for Processing 4",
//                  "scripts": {
//                    "test": "echo 'Node.js integration with Processing 4 is working!'",
//                    "build": "echo 'Building assets for Processing...'",
//                    "clean": "echo 'Cleaning build artifacts...'"
//                  },
//                  "devDependencies": {}
//                }
//            """.trimIndent())
//            println("Created test package.json")
//        } else {
//            println("package.json already exists")
//        }
//    }
//}
//
//// Test npm commands
//tasks.register("testNpmCommands") {
//    description = "Test npm commands with Processing"
//    dependsOn("createTestPackage", "npmInstall")
//    doLast {
//        println("NPM integration test completed!")
//    }
//}

tasks.register("createJsStructure") {
    description = "Create directory structure for JavaScript files"
    doLast {
        val jsDir = file("src/main/js")
        jsDir.mkdirs()
        println("Created directory: ${jsDir.absolutePath}")
    }
}

// Enhanced package.json creation with dependencies
tasks.register("createTestPackage") {
    description = "Create a test package.json for Processing"
    dependsOn("createJsStructure")
    doLast {
        val packageJson = file("package.json")
        if (!packageJson.exists()) {
            packageJson.writeText("""
                {
                  "name": "processing4-test",
                  "version": "1.0.0",
                  "description": "Node.js integration test for Processing 4",
                  "main": "src/main/js/index.js",
                  "scripts": {
                    "test": "node src/main/js/test.js",
                    "build": "echo 'Building assets for Processing...'",
                    "clean": "echo 'Cleaning build artifacts...'"
                  },
                  "dependencies": {
                    "lodash": "^4.17.21"
                  },
                  "devDependencies": {}
                }
            """.trimIndent())
            println("Created test package.json with lodash dependency")
        } else {
            println("package.json already exists")
        }
    }
}

// Install npm dependencies
tasks.register("installNodeDeps") {
    description = "Install Node.js dependencies"
    dependsOn("createTestPackage", "npmInstall")
    doLast {
        println("Node.js dependencies installed successfully!")
    }
}

// Test basic Node.js functionality
tasks.register("testNodeSetup") {
    description = "Test that Node.js plugin is working"
    dependsOn("nodeSetup")
    doLast {
        println("Node.js plugin setup completed successfully!")
    }
}

// Test npm commands
tasks.register("testNpmCommands") {
    description = "Test npm commands with Processing"
    dependsOn("installNodeDeps")
    doLast {
        println("NPM integration test completed!")
    }
}

// Test Node.js function calls from Java
tasks.register<JavaExec>("testNodeIntegration") {
    description = "Test Node.js integration with Java/Processing code"
    dependsOn("compileJava", "installNodeDeps")

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("processing.test.NodeIntegrationTest")

    doFirst {
        println("Testing Node.js integration with Processing...")
    }
}

// Compile and test everything
tasks.register("testAll") {
    description = "Run all tests for Node.js integration"
    dependsOn("testNodeSetup", "testNpmCommands", "testNodeIntegration")
    doLast {
        println("All integration tests completed successfully!")
    }
}