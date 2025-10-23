package processing.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import processing.app.ui.theme.LocalLocale
import processing.app.ui.theme.PDEComposeWindow
import processing.app.ui.theme.PDETheme

@Composable
fun PDEWelcome() {
    Row (modifier = Modifier.fillMaxSize()){
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(0.8f)
                .padding(32.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Language, contentDescription = "")
//                    Text(LocalLocale.current.locale.displayName, style = MaterialTheme.typography.labelSmall)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "")
                }
            }
            Row (horizontalArrangement = Arrangement.spacedBy(16.dp)){
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "Welcome to Processing!",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .align(Alignment.Bottom)
                )
            }
            Column {
                TextButton(onClick = {}) {
                    Icon(Icons.Default.Drafts, contentDescription = "")
                    Text("New Empty Sketch")
                }
                TextButton(onClick = {}) {
                    Icon(Icons.Default.Image, contentDescription = "")
                    Text("Open Examples")
                }
                TextButton(onClick = {}) {
                    Icon(Icons.Default.Folder, contentDescription = "")
                    Text("Sketchbook")
                }
            }
        }
        VerticalDivider()
        Column(modifier = Modifier
            .sizeIn(minWidth = 250.dp)
        ) {
            Text("Right Side Content", style = MaterialTheme.typography.bodyLarge)
        }
    }
}



fun main(){
    application {
        PDEComposeWindow(titleKey = "welcome.title", size = DpSize(800.dp, 600.dp), fullWindowContent = true) {
            PDETheme(darkTheme = false) {
                PDEWelcome()
            }
        }
    }
}