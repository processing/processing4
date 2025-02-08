package processing.app.ui.components.examples

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import processing.app.LocalPreferences
import processing.app.Messages
import processing.app.Platform
import java.awt.Cursor
import java.io.File
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory

data class Example(
    val folder: Path,
    val library: Path,
    val path: String = library.resolve("examples").relativize(folder).toString(),
    val title: String = folder.fileName.toString(),
    val image: Path = folder.resolve("$title.png")
)

@Composable
fun loadExamples(): List<Example> {
    val sketchbook = rememberSketchbookPath()
    val resources = File(System.getProperty("compose.application.resources.dir") ?: "")
    var examples by remember { mutableStateOf(emptyList<Example>()) }

    val settingsFolder = Platform.getSettingsFolder()
    val examplesCache = settingsFolder.resolve("examples.cache")
    LaunchedEffect(sketchbook, resources){
        if (!examplesCache.exists()) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            examples = examplesCache.readText().lines().map {
                val (library, folder) = it.split(",")
                Example(
                    folder = File(folder).toPath(),
                    library = File(library).toPath()
                )
            }
        }
    }

    LaunchedEffect(sketchbook, resources){
        withContext(Dispatchers.IO) {
            // TODO: Optimize
            Messages.log("Start scanning for examples in $sketchbook and $resources")
            //                  Folders that can contain contributions with examples
            val scanned = listOf("libraries", "examples", "modes")
                .flatMap { listOf(sketchbook.resolve(it), resources.resolve(it)) }
                .filter { it.exists() && it.isDirectory() }
                // Find contributions within those folders
                .flatMap { Files.list(it.toPath()).toList() }
                .filter { Files.isDirectory(it) }
                // Find examples within those contributions
                .flatMap { library ->
                    val fs = FileSystems.getDefault()
                    val matcher = fs.getPathMatcher("glob:**/*.pde")
                    val exampleFolders = mutableListOf<Path>()
                    val examples = library.resolve("examples")
                    if (!Files.exists(examples) || !examples.isDirectory()) return@flatMap emptyList()

                    Files.walkFileTree(library, object : SimpleFileVisitor<Path>() {
                        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                            if (matcher.matches(file)) {
                                exampleFolders.add(file.parent)
                            }
                            return FileVisitResult.CONTINUE
                        }
                    })
                    return@flatMap exampleFolders.map { folder ->
                        Example(
                            folder,
                            library,
                        )
                    }
                }
                .filter { it.image.exists() }
            Messages.log("Done scanning for examples in $sketchbook and $resources")
            if(scanned.isEmpty()) return@withContext
            examples = scanned
            examplesCache.writeText(examples.joinToString("\n") { "${it.library},${it.folder}" })
        }
    }

    return examples

}

@Composable
fun rememberSketchbookPath(): File {
    val preferences = LocalPreferences.current
    val sketchbookPath = remember(preferences["sketchbook.path.four"]) {
        preferences["sketchbook.path.four"] ?: Platform.getDefaultSketchbookFolder().toString()
    }
    return File(sketchbookPath)
}


@OptIn(ExperimentalResourceApi::class, ExperimentalComposeUiApi::class)
@Composable
fun examples(){
    val examples = loadExamples()
    // grab 4 random ones
    val randoms = examples.shuffled().take(4)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ){
        items(randoms){ example ->
            Column(
                modifier = Modifier
                    .onPointerEvent(PointerEventType.Press) {
                    }
                    .onPointerEvent(PointerEventType.Release) {
                    }
                    .onPointerEvent(PointerEventType.Enter) {
                    }
                    .onPointerEvent(PointerEventType.Exit) {
                    }
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
            ) {
                val imageBitmap: ImageBitmap = remember(example.image) {
                    example.image.inputStream().readAllBytes().decodeToImageBitmap()
                }
                Image(
                    painter = BitmapPainter(imageBitmap),
                    contentDescription = example.title,
                    modifier = Modifier
                        .background(colors.primary)
                        .width(185.dp)
                        .aspectRatio(16f / 9f)
                )
                Text(example.title)
            }
        }

    }
}

