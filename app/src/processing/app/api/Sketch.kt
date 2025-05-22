package processing.app.api

import kotlinx.serialization.Serializable
import java.io.File

class Sketch {
    companion object{
        @Serializable
        data class Sketch(
            val type: String = "sketch",
            val name: String,
            val path: String,
            val mode: String = "java",
        )

        @Serializable
        data class Folder(
            val type: String = "folder",
            val name: String,
            val path: String,
            val mode: String = "java",
            val children: List<Folder> = emptyList(),
            val sketches: List<Sketch> = emptyList()
        )

        fun getSketches(file: File, filter: (File) -> Boolean = { true }): Folder {
            val name = file.name
            val (sketchesFolders, childrenFolders) = file.listFiles()?.partition { isSketchFolder(it) } ?: return Folder(
                name = name,
                path = file.absolutePath,
                sketches = emptyList(),
                children = emptyList()
            )

            val children = childrenFolders.filter(filter) .map { getSketches(it) }
            val sketches = sketchesFolders.map { Sketch(name = it.name, path = it.absolutePath) }
            return Folder(
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
