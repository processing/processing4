package processing.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import processing.app.Base
import processing.app.LocalPreferences
import processing.app.Messages
import processing.app.Platform
import processing.app.api.Contributions.ExamplesList.Companion.listAllExamples
import processing.app.api.Sketch.Companion.Sketch
import processing.app.ui.preferences.Interface.Companion.languagesDropdown
import processing.app.ui.theme.LocalLocale
import processing.app.ui.theme.PDEComposeWindow
import processing.app.ui.theme.PDESwingWindow
import processing.app.ui.theme.PDETheme
import processing.app.ui.theme.toDimension
import java.io.File
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.exists

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PDEWelcome(base: Base? = null) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ){
        val xsPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        val xsModifier = Modifier
            .defaultMinSize(minHeight = 1.dp)
            .height(32.dp)
        val textColor = if(isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSecondaryContainer
        val locale = LocalLocale.current

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.8f)
                .padding(
                    top = 48.dp,
                    start = 56.dp,
                    end = 64.dp,
                    bottom = 56.dp
                )
        ) {
            Row (
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ){
                Image(
                    painter = painterResource("logo.svg"),
                    modifier = Modifier
                        .size(50.dp),
                    contentDescription = locale["welcome.processing.logo"]
                )
                Text(
                    text = locale["welcome.processing.title"],
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = textColor,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterVertically),
                    horizontalArrangement = Arrangement.End,
                ){
                    val showLanguageMenu = remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = {
                            showLanguageMenu.value = !showLanguageMenu.value
                        },
                        contentPadding = xsPadding,
                        modifier = xsModifier
                    ){
                        Icon(Icons.Default.Language, contentDescription = "", modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = locale.locale.displayName)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "", modifier = Modifier.size(20.dp))
                        languagesDropdown(showLanguageMenu)
                    }

                }

            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                val colors = ButtonDefaults.textButtonColors(
                    contentColor = textColor
                )
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                        val medModifier = Modifier
                            .sizeIn(minHeight = 56.dp)
                        TextButton(
                            onClick = {
                                base?.handleNew() ?: noBaseWarning()
                            },
                            colors = colors,
                            modifier = medModifier
                        ) {
                            Icon(Icons.Outlined.NoteAdd, contentDescription = "")
                            Spacer(Modifier.width(12.dp))
                            Text(locale["welcome.actions.sketch.new"])
                        }
                        TextButton(
                            onClick = {
                                base?.let{
                                    base.showExamplesFrame()
                                } ?: noBaseWarning()
                            },
                            colors = colors,
                            modifier = medModifier
                        ) {
                            Icon(Icons.Outlined.FolderSpecial, contentDescription = "")
                            Spacer(Modifier.width(12.dp))
                            Text(locale["welcome.actions.examples"] )
                        }
                        TextButton(
                            onClick = {
                                base?.let{
                                    base.showSketchbookFrame()
                                } ?: noBaseWarning()
                            },
                            colors = colors,
                            modifier = medModifier
                        ) {
                            Icon(Icons.Outlined.FolderOpen, contentDescription = "", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(locale["sketchbook"], modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ){
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier
                        .padding(
                            top = 18.dp,
                            end = 24.dp,
                            bottom = 24.dp,
                            start = 24.dp
                        )
                ) {
                    val colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                        Column {
                            Text(
                                text = locale["welcome.resources.title"],
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            TextButton(
                                onClick = {
                                    Platform.openURL("https://processing.org/tutorials/gettingstarted")
                                },
                                contentPadding = xsPadding,
                                modifier = xsModifier,
                                colors = colors
                            ) {
                                Icon(Icons.Outlined.PinDrop, contentDescription = "", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = locale["welcome.resources.get_started"],
                                )
                            }
                            TextButton(
                                onClick = {
                                    Platform.openURL("https://processing.org/tutorials")
                                },
                                contentPadding = xsPadding,
                                modifier = xsModifier,
                                colors = colors
                            ) {
                                Icon(Icons.Outlined.School, contentDescription = "", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = locale["welcome.resources.tutorials"],
                                )
                            }
                            TextButton(
                                onClick = {
                                    Platform.openURL("https://processing.org/reference")
                                },
                                contentPadding = xsPadding,
                                modifier = xsModifier,
                                colors = colors
                            ) {
                                Icon(Icons.Outlined.Book, contentDescription = "", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = locale["welcome.resources.documentation"],
                                )
                            }
                        }
                        Column {
                            Text(
                                text = locale["welcome.community.title"],
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Column {
                                    TextButton(
                                        onClick = {
                                            Platform.openURL("https://discourse.processing.org")
                                        },
                                        contentPadding = xsPadding,
                                        modifier = xsModifier,
                                        colors = colors
                                    ) {
                                        Icon(
                                            Icons.Outlined.ChatBubbleOutline,
                                            contentDescription = "",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = locale["welcome.community.forum"]
                                        )
                                    }
                                    TextButton(
                                        onClick = {
                                            Platform.openURL("https://discord.processing.org")
                                        },
                                        contentPadding = xsPadding,
                                        modifier = xsModifier,
                                        colors = colors
                                    ) {
                                        Icon(
                                            painterResource("icons/Discord.svg"),
                                            contentDescription = "",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Discord")
                                    }
                                }
                                Column {
                                    TextButton(
                                        onClick = {
                                            Platform.openURL("https://www.instagram.com/processing_core/")
                                        },
                                        contentPadding = xsPadding,
                                        modifier = xsModifier,
                                        colors = colors
                                    ) {
                                        Icon(
                                            painterResource("icons/GitHub.svg"),
                                            contentDescription = "",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("GitHub")
                                    }
                                    TextButton(
                                        onClick = {
                                            Platform.openURL("https://github.com/processing/processing4")
                                        },
                                        contentPadding = xsPadding,
                                        modifier = xsModifier,
                                        colors = colors
                                    ) {
                                        Icon(
                                            painterResource("icons/Instagram.svg"),
                                            contentDescription = "",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Instagram")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val preferences = LocalPreferences.current
            val showOnStartup = preferences["welcome.four.show"].toBoolean()
            fun toggle(next: Boolean? = null){
                preferences["welcome.four.show"] = (next ?: !showOnStartup).toString()
            }
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(onClick = ::toggle)
                    .padding(end = 8.dp)
                    .height(32.dp)
            ) {
                Checkbox(
                    checked = showOnStartup,
                    onCheckedChange = ::toggle,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.tertiary
                    )
                )
                Text(
                    text = locale["welcome.actions.show_startup"],
                    modifier = Modifier.align(Alignment.CenterVertically),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Column(modifier = Modifier
            .sizeIn(minWidth = 350.dp)
            .padding(end = 12.dp)
        ) {
            val examples = remember { mutableStateListOf(
                Sketch(name = "Array", path = Platform.getContentFile("modes/java/examples/Basics/Arrays/Array").absolutePath),
                Sketch(name = "Perspective", path = Platform.getContentFile("modes/java/examples/Basics/Camera/Perspective").absolutePath),
                Sketch(name = "Brightness", path = Platform.getContentFile("modes/java/examples/Basics/Color/Brightness").absolutePath),
                Sketch(name = "LoadDisplayOBJ", path = Platform.getContentFile("modes/java/examples/Basics/Shape/LoadDisplayOBJ").absolutePath),
            )}

            remember {
                val sketches = mutableListOf<Sketch>()
                val sketchFolders = listAllExamples()
                fun gatherSketches(folder: processing.app.api.Sketch.Companion.Folder?) {
                    if (folder == null) return
                    sketches.addAll(folder.sketches.filter { it -> Path(it.path).resolve("${it.name}.png").exists() })
                    folder.children.forEach { child ->
                        gatherSketches(child)
                    }
                }
                sketchFolders.forEach { folder ->
                    gatherSketches(folder)
                }
                if(sketches.isEmpty()) {
                    return@remember
                }
                examples.clear()
                examples.addAll(sketches.shuffled().take(20))
            }

            LazyColumn(
                state = rememberLazyListState(
                    initialFirstVisibleItemScrollOffset = 150
                ),
                modifier = Modifier
                    .width(350.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(examples) { example ->
                    var hovered by remember { mutableStateOf(false) }
                    Box(Modifier
                        .border(BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant), shape = MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                        .clip(MaterialTheme.shapes.medium)
                        .fillMaxSize()
                        .aspectRatio(16 / 9f)
                        .onPointerEvent(PointerEventType.Enter){
                            hovered = true
                        }
                        .onPointerEvent(PointerEventType.Exit){
                            hovered = false
                        }
                    ){
                        val image = remember {
                            File(example.path,"${example.name}.png").takeIf { it.exists() }
                        }
                        if(image == null){
                            Icon(
                                painter = painterResource("logo.svg"),
                                modifier = Modifier
                                    .size(75.dp)
                                    .align(Alignment.Center)
                                ,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                contentDescription = "Processing Logo"
                            )
                            HorizontalDivider()
                        }else {
                            val imageBitmap: ImageBitmap = remember(image) {
                                image.inputStream().readAllBytes().decodeToImageBitmap()
                            }
                            Image(
                                painter = BitmapPainter(imageBitmap),
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentDescription = example.name
                            )
                        }
                        Column(
                            modifier = Modifier.align(Alignment.BottomCenter),
                        ) {
                            val duration = 150
                            AnimatedVisibility(
                                visible = hovered,
                                enter = slideIn(
                                    initialOffset = { fullSize -> IntOffset(0, fullSize.height) },
                                    animationSpec = tween(
                                        durationMillis = duration,
                                        easing = EaseInOut
                                    )
                                ),
                                exit = slideOut (
                                    targetOffset = { fullSize -> IntOffset(0, fullSize.height) },
                                    animationSpec = tween(
                                        durationMillis = duration,
                                        easing = LinearEasing
                                    )
                                )
                            ) {
                                Card(
                                    modifier = Modifier
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min)
                                            .padding(12.dp)
                                            .padding(start = 12.dp)
                                    ) {
                                        Text(
                                            text = example.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .padding(8.dp)
                                        )
                                        Button(
                                            onClick = {
                                                base?.let {
                                                    base.handleOpen("${example.path}/${example.name}.pde")
                                                } ?: noBaseWarning()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.tertiary,
                                                contentColor = MaterialTheme.colorScheme.onTertiary
                                            )
                                        ) {
                                            Text(
                                                text = locale["welcome.sketch.open"],
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class Example(
    val path: File,
)

fun noBaseWarning() {
    Messages.showWarning(
        "No Base",
        "No Base instance provided, this ui is likely being previewed."
    )
}

val size = DpSize(970.dp, 600.dp)
val titleKey = "menu.help.welcome"

fun showWelcomeScreen(base: Base? = null) {
    PDESwingWindow(titleKey = titleKey, size = size.toDimension(), fullWindowContent = true) {
        PDEWelcome(base)
    }
}


fun main(){
    application {
        PDEComposeWindow(titleKey = titleKey, size = size, fullWindowContent = true) {
            PDETheme(darkTheme = true) {
                PDEWelcome()
            }
        }
        PDEComposeWindow(titleKey = titleKey, size = size, fullWindowContent = true) {
            PDETheme(darkTheme = false) {
                PDEWelcome()
            }
        }
    }
}