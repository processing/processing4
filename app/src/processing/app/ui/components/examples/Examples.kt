package processing.app.ui.components.examples

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
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
import processing.app.ui.Welcome.Companion.LocalBase
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



@Composable
fun examples(){
    val examples = loadExamples()


    var randoms = examples.shuffled().take(4)
    if(randoms.size < 4){
        randoms = randoms + List(4 - randoms.size) { Example(
            folder = Paths.get(""),
            library = Paths.get(""),
            title = "Test",
            image = ClassLoader.getSystemResource("default.png")?.toURI()?.let { Paths.get(it) } ?: Paths.get(""),
        ) }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        randoms.chunked(2).forEach { row ->
            Row (
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ){
                row.forEach { example ->
                    Example(example)
                }
            }
        }
    }
}
@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
@Composable
fun Example(example: Example){
    val base = LocalBase.current
    Button(
        onClick = {
            base?.handleOpenExample("${example.folder}/${example.title}.pde", base.defaultMode)
        },
        contentPadding = PaddingValues(0.dp),
        elevation = null,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            contentColor = colors.onBackground
        ),
    ) {
        Column(
            modifier = Modifier
                .width(185.dp)
        ) {
            val imageBitmap: ImageBitmap = remember(example.image) {
                example.image.inputStream().readAllBytes().decodeToImageBitmap()
            }
            Image(
                painter = BitmapPainter(imageBitmap),
                contentDescription = example.title,
                modifier = Modifier
                    .background(colors.primary)
                    .aspectRatio(16f / 9f)
            )
            Text(
                example.title,
                style = typography.body1,
                maxLines = 1
            )
        }
    }
}
