package processing.app.api

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import kotlinx.coroutines.*
import processing.app.Mode
import processing.app.contrib.ContributionType
import java.io.File
import java.nio.file.*
import kotlin.io.path.isDirectory
import kotlin.io.path.name

class Mode {
    companion object {
        /**
         * Find sketches in the given root folder for the specified mode.
         *
         * Based on ExamplesFrame.buildTree()
         *
         * @param root The root directory to search for sketches.
         * @param mode The mode to filter sketches by.
         * @return A list of sketch folders found in the root directory.
         */
        fun findExampleSketches(
            mode: Mode,
            sketchbookFolder: File? = null,
            scope: CoroutineScope? = null
        ): List<Sketch.Companion.Folder> {
            val baseExamples = mode.exampleCategoryFolders.mapNotNull {
                searchForSketches(it.toPath(), mode, { true }, scope)
            }

            val coreLibraryExamples = mode.coreLibraries.mapNotNull {
                searchForSketches(it.examplesFolder.toPath(), mode, { true }, scope).replace(
                    name = it.name,
                    path = it.path
                )
            }.wrap(
                name = "Libraries",
                path = File(mode.coreLibraries.first().path).parent.toString()
            )

            val contributedLibraryExamples = mode.contribLibraries.mapNotNull {
                searchForSketches(it.examplesFolder.toPath(), mode, { true }, scope).replace(
                    name = it.name,
                    path = it.path
                )
            }.wrap(
                name = "Contributed Libraries",
                path = File(mode.contribLibraries.firstOrNull()?.path ?: mode.getContentFile("").path).parent.toString()
            )

            val contributedExamplePacks = sketchbookFolder?.let { root ->
                ContributionType.EXAMPLES.listCandidates(root).mapNotNull {
                    searchForSketches(it.toPath(), mode, { true }, scope)
                }
            }
            return (baseExamples + coreLibraryExamples + contributedLibraryExamples + (contributedExamplePacks
                ?: emptyList()))
        }

        /**
         * Find sketches in the given root folder for the specified mode.
         *
         * Based on Base.addSketches()
         *
         * @param root The root directory to search for sketches.
         * @param mode The mode to filter sketches by.
         * @return A list of sketch folders found in the root directory.
         */
        fun searchForSketches(
            root: Path,
            mode: Mode,
            filter: ((Path) -> Boolean) = { true },
            scope: CoroutineScope? = null
        ): Sketch.Companion.Folder? {
            if (!root.isDirectory()) return null
            if (!filter(root)) return null

            val stream = Files.newDirectoryStream(root)
            val (sketchFolders, subfolders) = stream
                .filter { path -> path.isDirectory() }
                .filter { path -> filter(path) }
                .partition { path ->
                    val main = processing.app.Sketch.findMain(path.toFile(), listOf(mode))
                    main != null
                }
            val sketches = sketchFolders.map {
                Sketch.Companion.Sketch(
                    name = it.fileName.toString(),
                    path = it.toString(),
                    mode = mode.identifier
                )
            }.toMutableStateList()
            val children = subfolders.mapNotNull {
                searchForSketches(it, mode, filter)
            }.toMutableStateList()
            if (sketches.isEmpty() && children.isEmpty()) return null

            scope?.launch(Dispatchers.IO) {
                val watchService: WatchService = FileSystems
                    .getDefault()
                    .newWatchService()

                val watcher = root.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
                while (isActive) {
                    delay(100)
                    watchService.poll() ?: continue

                    watcher.pollEvents().forEach { _ ->
                        val updatedFolder = searchForSketches(root, mode, filter) ?: return@forEach

                        sketches.clear()
                        sketches.addAll(updatedFolder.sketches)
                        children.clear()
                        children.addAll(updatedFolder.children)
                    }

                }
            }

            return Sketch.Companion.Folder(
                name = root.name,
                path = root.toString(),
                sketches = sketches,
                children = children
            )
        }

        fun Sketch.Companion.Folder?.wrap(
            name: String? = this?.name,
            path: String? = this?.path
        ): Sketch.Companion.Folder? {
            if (this == null) return null;
            return Sketch.Companion.Folder(
                name = name ?: this.name,
                path = path ?: this.path,
                sketches = mutableStateListOf(),
                children = mutableStateListOf(this)
            )
        }

        fun List<Sketch.Companion.Folder>.wrap(
            name: String,
            path: String,
        ): List<Sketch.Companion.Folder> {
            if (this.isEmpty()) return emptyList()
            return listOf(
                Sketch.Companion.Folder(
                    name = name,
                    path = path,
                    sketches = mutableStateListOf(),
                    children = this.toMutableStateList()
                )
            )
        }

        fun Sketch.Companion.Folder?.replace(
            name: String? = this?.name,
            path: String? = this?.path
        ): Sketch.Companion.Folder? {
            if (this == null) return null;
            return Sketch.Companion.Folder(
                name = name ?: this.name,
                path = path ?: this.path,
                sketches = this.sketches,
                children = this.children
            )
        }
    }
}