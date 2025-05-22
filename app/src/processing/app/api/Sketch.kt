package processing.app.api

import java.io.File

class Sketch {
    companion object{
        fun getSketches(file: File): Contributions.ExamplesList.Folder {
            val name = file.name
            val (sketchesFolders, childrenFolders) = file.listFiles().partition { isSketchFolder(it) }

            val children = childrenFolders.map { getSketches(it) }
            val sketches = sketchesFolders.map { Contributions.ExamplesList.Sketch(name = it.name, path = it.absolutePath) }
            return Contributions.ExamplesList.Folder(
                name = name,
                path = file.absolutePath,
                children = children,
                sketches = sketches
            )
        }
        fun isSketchFolder(file: File): Boolean {
            return file.isDirectory && file.listFiles().any { it.isFile && it.name.endsWith(".pde") }
        }
    }
}
