# Processing Java Mode

This the Java Mode in Processing. It compiles your sketches and runs them. It is the primary mode of Processing.

## Folders
- `application` assets for exporting applications within the mode (Deprecated)
- `generated` generated antlr code for the mode, should be moved to a proper `antlr` plugin within gradle (Deprecated)
- `gradle` the Processing java gradle plugin
- `libraries` libraries that are available within the mode
- `lsp` gradle build system for the language server protocol, in the future we should decouple the lsp from the java mode and pde and move all relevant code here. For now it can be found in `src/.../lsp`
- `mode` legacy files for `Ant`
- `preprocessor` the preprocessor for the mode, same deal as with the lsp, although the decoupling has mostly been done
- `src` the main source code for the mode
- `test` tests for the mode
- `theme` assets for the mode, related to autocomplete and syntax highlighting

## The Modern Build system

Since 2025 work has started on creating a new internal build system for the Java Mode based on Gradle.
The goal is to simplify by leaning more on Gradle, which provides a lot of the functionality that was build before out of the box and a lot more.

### How it used to work

The build system used to be based on some parts Ant, some parts eclipse (org.eclipse.jdt.core) and a lot of custom work build up over the years.

### How it will work going forward

The modern build system is based around Gradle, the main service (GradleService) for building a sketch with Gradle is included in `app` instead of into the Java mode as future modes are most likely also based on Gradle if they use `core` in some way. Most _Modes_ should/could probably be a Gradle plugin going forward.
Breaking the build system away from the java mode will mean that we create an island of isolation when it comes to the build system, allowing contributors to work on the build system without running the editor.
Another upside is that when we publish the Gradle plugin to the Gradle Plugin repository, it will become trivial to run Processing sketches outside the PDE and improvements made to the build system will be usable for everyone. 
There is now also an opportunity for creating contributions that modify the build system in more subtle ways rather than having to make a complete new mode, e.g. a compilation step for shaders or some setup tweaks to make JavaFX work out of the box.
Furthermore, this change will embed Processing more into the wider Java ecosystem, if users want to upgrade from using Processing to Java whilst still using `core` that will become possible and won't  need a rewrite of what they already created. 

### How to work on the modern build system

If you want to work on the build system without the PDE, open `/java/gradle/example` into a new intellij IDEA window, this is set up to compile the Processing Java plugin and run sketches standalone.

Within the editor, the gradle plugin is embedded in Processing's embedded maven repository so that Gradle can find it.  