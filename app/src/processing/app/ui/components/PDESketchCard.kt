package processing.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import processing.app.api.Sketch
import processing.app.ui.theme.LocalLocale
import java.io.File
import kotlin.io.inputStream

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun Sketch.Companion.Sketch.exampleCard(onOpen: () -> Unit = {}) {
    val locale = LocalLocale.current
    Column(
        modifier = Modifier.Companion
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onOpen)
            .padding(12.dp)
    ) {
        Box(
            Modifier.Companion
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = MaterialTheme.shapes.medium
                )
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = MaterialTheme.shapes.medium
                )
                .clip(MaterialTheme.shapes.medium)
                .fillMaxSize()
                .aspectRatio(16 / 9f)
        ) {
            val image = remember {
                File(path, "${name}.png").takeIf { it.exists() }
            }
            if (image == null) {
                Icon(
                    painter = painterResource("logo.svg"),
                    modifier = Modifier.Companion
                        .size(75.dp)
                        .align(Alignment.Companion.Center),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    contentDescription = "Processing Logo"
                )
                HorizontalDivider()
            } else {
                val imageBitmap: ImageBitmap = remember(image) {
                    image.inputStream().readAllBytes().decodeToImageBitmap()
                }
                Image(
                    painter = BitmapPainter(imageBitmap),
                    modifier = Modifier.Companion
                        .fillMaxSize(),
                    contentDescription = name
                )
            }

        }
        Text(name)
    }
}