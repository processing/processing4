package processing.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.formdev.flatlaf.util.SystemInfo
import processing.app.*
import processing.app.ui.components.LanguageChip
import processing.app.ui.components.examples.examples
import processing.app.ui.theme.*
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.nio.file.*
import java.util.*
import javax.swing.SwingUtilities


class Welcome @Throws(IOException::class) constructor(base: Base) {
    init {
        SwingUtilities.invokeLater {
            PDEWindow("menu.help.welcome", fullWindowContent = true) {
                CompositionLocalProvider(LocalBase provides base) {
                    welcome()
                }
            }
        }
    }

    companion object {
        val LocalBase = compositionLocalOf<Base?> { null }
        @Composable
        fun welcome() {
            Column(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colorStops = arrayOf(0f to Color.Transparent, 1f to Color("#C0D7FF".toColorInt())),
                            start = Offset(815f, 0f),
                            end = Offset(815f * 2, 450f)
                        )
                    )
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp)
                    .padding(top = if (SystemInfo.isMacFullWindowContentSupported) 22.dp else 0.dp)
                    .height(IntrinsicSize.Max)
                    .width(IntrinsicSize.Max)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .align(Alignment.End)
                ) {
                    LanguageChip()
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                ) {
                    Column {
                        intro()
                    }
                    Box{
                        Column {
                            examples()
                            actions()
                        }
                        val locale = LocalLocale.current
                        Image(
                            painter = painterResource("welcome/intro/wavy.svg"),
                            contentDescription = locale["welcome.intro.long"],
                            modifier = Modifier
                                .height(200.dp)
                                .offset (32.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }

        @Composable
        fun intro(){
            val locale = LocalLocale.current
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(IntrinsicSize.Max)
            ) {
                Column {
                    Text(
                        text = locale["welcome.intro.title"],
                        style = typography.h4,
                        modifier = Modifier
                            .sizeIn(maxWidth = 305.dp)
                    )
                    Text(
                        text = locale["welcome.intro.message"],
                        style = typography.body1,
                        modifier = Modifier
                            .sizeIn(maxWidth = 305.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .offset(y = 32.dp)
                ){
                    Text(
                        text = locale["welcome.intro.suggestion"],
                        style = typography.body1,
                        color = colors.onPrimary,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primary)
                            .padding(horizontal = 24.dp)
                            .padding(top = 16.dp, bottom = 24.dp)
                            .sizeIn(maxWidth = 200.dp)
                    )
                    Image(
                        painter = painterResource("welcome/intro/bubble.svg"),
                        contentDescription = locale["welcome.intro.long"],
                        modifier = Modifier
                            .align{ _, space, _ -> space / 4 }
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.
                            fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource("welcome/intro/long.svg"),
                            contentDescription = locale["welcome.intro.long"],
                            modifier = Modifier
                                .offset(x = -32.dp)
                        )
                        Image(
                            painter = painterResource("welcome/intro/short.svg"),
                            contentDescription = locale["welcome.intro.short"],
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .offset(x = 16.dp, y = -16.dp)
                        )
                    }
                }
            }
        }

        @Composable
        fun actions(){
            val locale = LocalLocale.current
            val base = LocalBase.current
            PDEChip(onClick = {
                base?.defaultMode?.showExamplesFrame()
            }) {
                Text(
                    text = locale["welcome.action.examples"],
                )
                Image(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = locale["welcome.action.tutorials"],
                    colorFilter = ColorFilter.tint(color = LocalContentColor.current),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(typography.body1.fontSize.value.dp)
                )
            }
            PDEChip(onClick = {
                if (!Desktop.isDesktopSupported()) return@PDEChip
                val desktop = Desktop.getDesktop()
                if(!desktop.isSupported(Desktop.Action.BROWSE)) return@PDEChip
                try {
                    desktop.browse(URI(System.getProperty("processing.tutorials")))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }) {
                Text(
                    text = locale["welcome.action.tutorials"],
                )
                Image(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = locale["welcome.action.tutorials"],
                    colorFilter = ColorFilter.tint(color = LocalContentColor.current),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(typography.body1.fontSize.value.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .offset(-32.dp)
                ) {
                    val preferences = LocalPreferences.current
                    Checkbox(
                        checked = preferences["welcome.four.show"]?.equals("true") ?: false,
                        onCheckedChange = {
                            preferences.setProperty("welcome.four.show", it.toString())
                        },
                        modifier = Modifier
                            .size(24.dp)
                    )
                    Text(
                        text = locale["welcome.action.startup"],
                    )
                }
                PDEButton(onClick = { println("Open") }) {
                    Text(
                        text = locale["welcome.action.go"],
                        modifier = Modifier
                    )
                }
            }
        }



        @JvmStatic
        fun main(args: Array<String>) {
            pdeapplication("menu.help.welcome", fullWindowContent = true) {
                welcome()
            }
        }
    }
}