# Processing Gradle Plugin

This folder contains the source for the Processing Gradle plugin.
The plugin will transform any Processing sketch into a Gradle project for easy compilation and advanced features.

## Motivation

Processing was designed to be easy to start with, and the PDE (Processing Development Environment) handles most things
for you: you can write code, import libraries, run your sketch, or even export it as an executable. This works very well
for learning and for small to medium sketches, but it isn’t ideal for larger projects.

With the Processing Gradle Plugin, we want to make it possible to build more ambitious projects on top of Processing.
This is intended for users who are comfortable moving beyond the PDE, such as artists and developers working on larger
sketches, long running installations, multi sketch projects, or teams who want version control, automated builds, and
integration with standard Java tools and editors. It is optional and does not replace the PDE, but complements it for
more advanced workflows.

## What is Gradle

Gradle is a build tool commonly used in the Java ecosystem. It is responsible for tasks like compiling code, managing
dependencies, and running applications. You do not need to learn Gradle to use Processing in the P

## Usage

Add the following files to any Processing sketch alongside the `.pde` files

`build.gradle.kts`
```kotlin
plugins {
    id("org.processing.java") version "4.5.3"
}
```

The version number determines which version of Processing will be used.

`settings.gradle.kts`
create the file but leave blank

This will turn the Processing sketch into a Gradle project, usable with any editor that supports Gradle.
Including the `gradle` command if installed. If you want to use your own editor, or no editor at all, use the
gradle command if installed. Find installation instructions
here: https://docs.gradle.org/current/userguide/installation.html

The plugin will add the `sketch` command to the Gradle tasks lists, so run the sketch with `gradle sketch`, this will
build and launch your sketch.

The sketch can also be bundled into a standalone app by using the `gradle export` command.
Or run in fullscreen with `gradle present`

To include libraries into your sketch add `processing.sketchbook=/path/to/sketchbook` to a `gradle.properties` file in
the same folder.

To use any kind of dependency add as a normal gradle dependency, the plugin has already automatically added the Maven
Central repository.

`build.gradle.kts`
```kotlin
plugins {
    id("org.processing.java") version "4.5.3"
}

dependencies {
    implementation("com.lowagie:itext:2.1.7")
}
```

To use an older version of Processing just change the plugin version:

`build.gradle.kts`
```kotlin
plugins {
    id("org.processing.java") version "4.5.0"
}
```

Other gradle plugins are also supported

`build.gradle.kts`
```kotlin
plugins {
    id("org.processing.java") version "4.5.3"
    id("com.gradleup.shadow") version "<version>"
}
```

If you want to combine multiple sketches into a single project

`sketch-a/build.gradle.kts`
```kotlin
plugins {
    id("org.processing.java") version "4.5.3"
}
```

`sketch-b/build.gradle.kts`

```kotlin
plugins {
    id("org.processing.java") version "4.5.3"
}
```

`build.gradle.kts`

```kotlin
plugins {
    id("org.processing.java") version "4.5.3" apply false
}
```

`settings.gradle.kts` - create the file but leave blank

Then run all sketches at once with `gradle sketch`