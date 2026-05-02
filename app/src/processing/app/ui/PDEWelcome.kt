package processing.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import processing.app.*
import processing.app.api.Contributions.ExamplesList.Companion.listAllExamples
import processing.app.api.Sketch.Companion.Sketch
import processing.app.ui.theme.*
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.exists

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PDEWelcome(base: Base? = null) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
    ){
        val shape = RoundedCornerShape(12.dp)
        val xsPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        val xsModifier = Modifier
            .defaultMinSize(minHeight = 1.dp)
            .height(32.dp)
        val textColor = if(isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSecondaryContainer
        val locale = LocalLocale.current

        /**
         * Left main column
         */
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
            /**
             * Title row
             */
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterVertically),
                    horizontalArrangement = Arrangement.End,
                ){
                    val showLanguageMenu = remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = {
                            showLanguageMenu.value = !showLanguageMenu.value
                        },
                        contentPadding = xsPadding,
                        modifier = xsModifier,
                        shape = shape
                    ){
                        Icon(Icons.Default.Language, contentDescription = "", modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = locale.locale.displayName)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "", modifier = Modifier.size(20.dp))
                        languagesDropdown(showLanguageMenu)
                    }
                }
            }
            /**
             * New sketch, examples, sketchbook card
             */
            val colors = ButtonDefaults.textButtonColors(
                contentColor = textColor
            )
            Column{
                ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                    val medModifier = Modifier
                        .sizeIn(minHeight = 56.dp)
                    TextButton(
                        onClick = {
                            base?.handleNew() ?: noBaseWarning()
                        },
                        colors = colors,
                        modifier = medModifier,
                        shape = shape
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.NoteAdd, contentDescription = "")
                        Spacer(Modifier.width(12.dp))
                        Text(locale["welcome.actions.sketch.new"])
                    }
                    TextButton(
                        onClick = {
                            base?.let{
                                base.showSketchbookFrame()
                            } ?: noBaseWarning()
                        },
                        colors = colors,
                        modifier = medModifier,
                        shape = shape
                    ) {
                        Icon(Icons.Outlined.FolderOpen, contentDescription = "")
                        Spacer(Modifier.width(12.dp))
                        Text(locale["welcome.actions.sketchbook"], modifier = Modifier.align(Alignment.CenterVertically))
                    }
                    TextButton(
                        onClick = {
                            base?.let{
                                base.showExamplesFrame()
                            } ?: noBaseWarning()
                        },
                        colors = colors,
                        modifier = medModifier,
                        shape = shape
                    ) {
                        Icon(Icons.Outlined.FolderSpecial, contentDescription = "")
                        Spacer(Modifier.width(12.dp))
                        Text(locale["welcome.actions.examples"])
                    }
                }
            }
            /**
             * Resources and community card
             */
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
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
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
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
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
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = locale["welcome.community.title"],
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(48.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
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
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    TextButton(
                                        onClick = {
                                            Platform.openURL("https://github.com/processing/processing4")
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
                                            Platform.openURL("https://www.instagram.com/processing_core/")
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
            /**
             * Show on startup checkbox
             */
            Row{
                val preferences = LocalPreferences.current
                val showOnStartup = preferences["welcome.four.show"].toBoolean()
                fun toggle(next: Boolean? = null) {
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
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 1.dp)
                    )
                    Text(
                        text = locale["welcome.actions.show_startup"],
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        /**
         * Examples list
         */
        val scrollMargin = 35.dp
        Column(
            modifier = Modifier
                .width(350.dp + scrollMargin)
        ) {
            val examples = remember {
                mutableStateListOf(
                    *listOf(
                        Platform.getContentFile("modes/java/examples/Basics/Arrays/Array"),
                        Platform.getContentFile("modes/java/examples/Basics/Camera/Perspective"),
                        Platform.getContentFile("modes/java/examples/Basics/Color/Brightness"),
                        Platform.getContentFile("modes/java/examples/Basics/Shape/LoadDisplayOBJ")
                    ).map { Sketch(path = it.absolutePath, name = it.name) }.toTypedArray()
                )
            }

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
                if (sketches.isEmpty()) {
                    return@remember
                }
                examples.clear()
                examples.addAll(sketches.shuffled().take(20))
            }
            val state = rememberLazyListState(
                initialFirstVisibleItemScrollOffset = 150
            )
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
            ) {
                LazyColumn(
                    state = state,
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp, end = 20.dp, start = scrollMargin),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(examples) { example ->
                        example.card{
                            base?.let {
                                base.handleOpen("${example.path}/${example.name}.pde")
                            } ?: noBaseWarning()
                        }
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd),
                    adapter = rememberScrollbarAdapter(state)
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun Sketch.card(onOpen: () -> Unit = {}) {
    val locale = LocalLocale.current
    val sketch = this
    var hovered by remember { mutableStateOf(false) }
    Box(
        Modifier
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = MaterialTheme.shapes.medium
            )
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .fillMaxSize()
            .aspectRatio(16 / 9f)
            .onPointerEvent(PointerEventType.Enter) {
                hovered = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                hovered = false
            }
    ) {
        val image = remember {
            File(sketch.path, "${sketch.name}.png").takeIf { it.exists() }
        }
        if (image == null) {
            Icon(
                painter = painterResource("logo.svg"),
                modifier = Modifier
                    .size(75.dp)
                    .align(Alignment.Center),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                contentDescription = "Processing Logo"
            )
            HorizontalDivider()
        } else {
            val imageBitmap: ImageBitmap = remember(image) {
                image.inputStream().readAllBytes().decodeToImageBitmap()
            }
            Image(
                painter = BitmapPainter(imageBitmap),
                modifier = Modifier
                    .fillMaxSize(),
                contentDescription = sketch.name
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
                exit = slideOut(
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
                            text = sketch.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(8.dp)
                        )
                        Button(
                            onClick = onOpen,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            contentPadding = PaddingValues(
                                horizontal = 12.dp,
                                vertical = 4.dp
                            ),
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

fun noBaseWarning() {
    Messages.showWarning(
        "No Base",
        "No Base instance provided, this ui is likely being previewed."
    )
}

val size = DpSize(970.dp, 600.dp)
const val titleKey = "menu.help.welcome"
class WelcomeScreen

fun showWelcomeScreen(base: Base? = null) {
    PDESwingWindow(
        titleKey = titleKey,
        size = size.toDimension(),
        unique = WelcomeScreen::class,
        fullWindowContent = true
    ) {
        PDEWelcome(base)
    }
}

@Composable
fun languagesDropdown(showOptions: MutableState<Boolean>) {
    val locale = LocalLocale.current
    val languages = if (Preferences.isInitialized()) Language.getLanguages() else mapOf("en" to "English")
    DropdownMenu(
        expanded = showOptions.value,
        onDismissRequest = {
            showOptions.value = false
        },
    ) {
        languages.forEach { family ->
            DropdownMenuItem(
                text = { Text(family.value) },
                onClick = {
                    locale.set(java.util.Locale(family.key))
                    showOptions.value = false
                }
            )
        }
    }
}

@Composable
fun PDEWelcomeWithSurvey(base: Base? = null) {
    Box {
        PDEWelcome(base)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 12.dp)
                .shadow(
                    elevation = 5.dp,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            SurveyInvitation()
        }
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