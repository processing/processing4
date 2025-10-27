package processing.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import processing.app.ui.theme.PDEComposeWindow
import processing.app.ui.theme.PDESwingWindow
import processing.app.ui.theme.PDETheme
import java.awt.Dimension

@Composable
fun PDEWelcome() {
    Row(
        modifier = Modifier.fillMaxSize(),
    ){
        val xsPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        val xsModifier = Modifier
            .defaultMinSize(minHeight = 1.dp)
            .height(32.dp)

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
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
//                    .background(Color.Blue)
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(vertical = 12.dp)
            ) {
                val colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                        TextButton(
                            onClick = {},
                            colors = colors,
                            modifier = Modifier
                                .sizeIn(minHeight = 56.dp)
                        ) {
                            Icon(Icons.Default.Drafts, contentDescription = "")
                            Spacer(Modifier.width(12.dp))
                            Text("New Empty Sketch")
                        }
                        TextButton(
                            onClick = {},
                            colors = colors,
                            modifier = Modifier
                                .sizeIn(minHeight = 56.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "")
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
                            onClick = {},
                            contentPadding = xsPadding,
                            colors = colors,
                            modifier = xsModifier
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = "")
                            Spacer(Modifier.width(12.dp))
                            Text("Sketchbook", modifier = Modifier.align(Alignment.CenterVertically))
                        }
                        TextButton(
                            onClick = {},
                            contentPadding = xsPadding,
                            colors = colors,
                            modifier = xsModifier
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = "")
                            Spacer(Modifier.width(12.dp))
                            Text("Settings", modifier = Modifier.align(Alignment.CenterVertically))
                        }
                        Button(
                            onClick = {},
                            contentPadding = xsPadding,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            modifier = xsModifier
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = "")
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
                    ProvideTextStyle(MaterialTheme .typography.labelLarge) {
                        Column {
                            Text(
                                "Resources",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            TextButton(
                                onClick = {},
                                contentPadding = xsPadding,
                                modifier = xsModifier,
                                colors = colors
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Video Course")
                            }
                            TextButton(
                                onClick = {},
                                contentPadding = xsPadding,
                                modifier = xsModifier,
                                colors = colors
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Get Started")
                            }
                            TextButton(
                                onClick = {},
                                contentPadding = xsPadding,
                                modifier = xsModifier,
                                colors = colors
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Tutorials")
                            }
                            TextButton(
                                onClick = {},
                                contentPadding = xsPadding,
                                modifier = xsModifier,
                                colors = colors
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "", modifier = Modifier.size(20.dp))
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
                            TextButton(
                                onClick = {},
                                contentPadding = xsPadding,
                                modifier = xsModifier,
                                colors = colors
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Video Course")
                            }
                            TextButton(
                                onClick = {},
                                contentPadding = xsPadding,
                                modifier = xsModifier,
                                colors = colors
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Get Started")
                            }
                            TextButton(
                                onClick = {},
                                contentPadding = xsPadding,
                                modifier = xsModifier,
                                colors = colors
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Tutorials")
                            }
                            TextButton(
                                onClick = {},
                                contentPadding = xsPadding,
                                modifier = xsModifier,
                                colors = colors
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Documentation")
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


fun showWelcomeScreen(){
    PDESwingWindow(titleKey = "welcome.title", size = Dimension(970, 570), fullWindowContent = true) {
        PDEWelcome()
    }
}


fun main(){
    application {
        PDEComposeWindow(titleKey = "welcome.title", size = DpSize(970.dp, 570.dp), fullWindowContent = true) {
            PDETheme(darkTheme = false) {
                PDEWelcome()
            }
        }
    }
}