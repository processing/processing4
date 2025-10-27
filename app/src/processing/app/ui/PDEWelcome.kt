package processing.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import processing.app.Base
import processing.app.LocalPreferences
import processing.app.Messages
import processing.app.Platform
import processing.app.ui.theme.PDEComposeWindow
import processing.app.ui.theme.PDESwingWindow
import processing.app.ui.theme.PDETheme
import processing.app.ui.theme.toDimension

@Composable
fun PDEWelcome(base: Base? = null) {
    Row(
        modifier = Modifier.fillMaxSize(),
    ){
        val xsPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        val xsModifier = Modifier
            .defaultMinSize(minHeight = 1.dp)
            .height(32.dp)
        val textColor = if(isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSecondaryContainer

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
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ){
                Image(
                    painter = painterResource("logo.svg"),
                    modifier = Modifier
                        .size(75.dp),
                    contentDescription = "Processing Logo"
                )
                Text(
                    text = "Welcome to Processing!",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = textColor,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(vertical = 12.dp)
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
                        TextButton(
                            onClick = {
                                base?.handleNew() ?: noBaseWarning()
                            },
                            colors = colors,
                            modifier = Modifier
                                .sizeIn(minHeight = 56.dp)
                        ) {
                            Icon(Icons.Outlined.NoteAdd, contentDescription = "")
                            Spacer(Modifier.width(12.dp))
                            Text("New Empty Sketch")
                        }
                        TextButton(
                            onClick = {
                                base?.let{
                                    base.showExamplesFrame()
                                } ?: noBaseWarning()
                            },
                            colors = colors,
                            modifier = Modifier
                                .sizeIn(minHeight = 56.dp)
                        ) {
                            Icon(Icons.Outlined.FolderSpecial, contentDescription = "")
                            Spacer(Modifier.width(12.dp))
                            Text("Open Examples")
                        }
                    }
                }
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                        TextButton(
                            onClick = {
                                base?.let{
                                    base.showSketchbookFrame()
                                } ?: noBaseWarning()
                            },
                            contentPadding = xsPadding,
                            colors = colors,
                            modifier = xsModifier
                        ) {
                            Icon(Icons.Outlined.FolderOpen, contentDescription = "", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Sketchbook", modifier = Modifier.align(Alignment.CenterVertically))
                        }
                        TextButton(
                            onClick = {
                                base?.let{
                                    base.handlePrefs()
                                } ?: noBaseWarning()
                            },
                            contentPadding = xsPadding,
                            colors = colors,
                            modifier = xsModifier
                        ) {
                            Icon(Icons.Outlined.Settings, contentDescription = "", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Settings", modifier = Modifier.align(Alignment.CenterVertically))
                        }
                        val preferences = LocalPreferences.current
                        val showOnStartup = preferences["welcome.four.show"].toBoolean()
                        Button(
                            onClick = {
                                preferences["welcome.four.show"] = (!showOnStartup).toString()
                            },
                            contentPadding = xsPadding,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if(showOnStartup) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = if (showOnStartup) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = xsModifier
                        ) {
                            Icon(if(showOnStartup) Icons.Default.Check else Icons.Default.Close, contentDescription = "", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Show this window on startup", modifier = Modifier.align(Alignment.CenterVertically))
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
                                "Resources",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            TextButton(
                                onClick = {
                                    Platform.openURL("https://hello.processing.org")
                                },
                                contentPadding = xsPadding,
                                modifier = xsModifier,
                                colors = colors
                            ) {
                                Icon(Icons.Outlined.SmartDisplay, contentDescription = "", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Video Course")
                            }
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
                                Text("Get Started")
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
                                Text("Tutorials")
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
                                Text("Documentation")
                            }
                        }
                        Column {
                            Text(
                                "Join our community",
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
                                        Text("Forum")
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
        }
        VerticalDivider()
        Column(modifier = Modifier
            .sizeIn(minWidth = 350.dp)
        ) {
            Text("Right Side Content", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

fun noBaseWarning() {
    Messages.showWarning(
        "No Base",
        "No Base instance provided, this ui is likely being previewed."
    )
}

val size = DpSize(970.dp, 550.dp)

fun showWelcomeScreen(base: Base? = null) {
    PDESwingWindow(titleKey = "welcome.title", size = size.toDimension(), fullWindowContent = true) {
        PDEWelcome(base)
    }
}


fun main(){
    application {
        PDEComposeWindow(titleKey = "welcome.title", size = size, fullWindowContent = true) {
            PDETheme(darkTheme = true) {
                PDEWelcome()
            }
        }
        PDEComposeWindow(titleKey = "welcome.title", size = size, fullWindowContent = true) {
            PDETheme(darkTheme = false) {
                PDEWelcome()
            }
        }
    }
}