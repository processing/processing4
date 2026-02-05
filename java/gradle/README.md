# Processing Gradle Plugin

This folder contains the source for the Processing Gradle plugin.
The plugin will transform any Processing sketch into a Gradle project for easy compilation and advanced features.

## Usage

Add the following files to any Processing sketch alongside the `.pde` files

```kotlin
// build.gradle.kts
plugins {
    id("org.processing.java") version "4.5.3" // version of Processing you would like to use.
}

// settings.gradle.kts - create the file but leave blank
```

This will make the Processing sketch a fully fledges Gradle project, usable with any editor that supports gradle.
Including the `gradle` command if installed.

The plugin will add the `sketch` command to the Gradle tasks lists, so run the sketch with `gradle sketch`, this will
build and launch your sketch.

The sketch can also be bundled into a standalone app by using the `gradle export` command.
Or run in fullscreen with `gradle present`

To include libraries into your sketch add `processing.sketchbook=/path/to/sketchbook` to a `gradle.properties` file in
the same folder.

To use any kind of dependency add as a normal gradle dependency, the plugin has already automatically added the Maven
Central repository.

```kotlin
// build.gradle.kts
plugins {
    id("org.processing.java") version "4.5.3"
}

dependencies {
    implementation("com.lowagie:itext:2.1.7")
}
```

To use an older version of Processing just change the plugin version:

```kotlin
// build.gradle.kts
plugins {
    id("org.processing.java") version "4.5.0"
}
```

Other gradle plugins are also supported

```kotlin
// build.gradle.kts
plugins {
    id("org.processing.java") version "4.5.3"
    id("com.gradleup.shadow") version "<version>"
}
```

If you want to combine multiple sketches into a single project

```kotlin
// sketch-a/build.gradle.kts
plugins {
    id("org.processing.java") version "4.5.3"
}

// sketch-b/build.gradle.kts
plugins {
    id("org.processing.java") version "4.5.3"
}

// build.gradle.kts
plugins {
    id("org.processing.java") version "4.5.3" apply false
}
// settings.gradle.kts - create the file but leave blank
```

Then run all sketches at once with `gradle sketch`