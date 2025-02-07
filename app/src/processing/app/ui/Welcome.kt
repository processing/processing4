package processing.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import processing.app.Base
import processing.app.LocalPreferences
import processing.app.ui.theme.LocalLocale
import processing.app.ui.theme.PDEButton
import processing.app.ui.theme.PDEWindow
import processing.app.ui.theme.pdeapplication
import java.io.IOException
import javax.swing.SwingUtilities

class Welcome @Throws(IOException::class) constructor(base: Base) {
    init {
        SwingUtilities.invokeLater {
            PDEWindow("menu.help.welcome") {
                welcome()
            }
        }
    }
    companion object {
        @Composable
        fun welcome() {
            Row(
                modifier = Modifier
//                    .background(
//                        Brush.linearGradient(
//                            colorStops = arrayOf(0.0f to Color.Transparent, 1f to Color.Blue),
//                            start = Offset(815f / 2, 0f),
//                            end = Offset(815f, 450f)
//                        )
//                    )
                    .size(815.dp, 450.dp)
                    .padding(32.dp)

                ,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ){
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                ){
                    intro()
                }
                Column(modifier = Modifier
                    .weight(1.25f)
                    .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ){
                    examples()
                    val locale = LocalLocale.current
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        chip {
                            Text(
                                text = locale["welcome.action.examples"],
                            )
                        }
                        chip {
                            Text(
                                text = locale["welcome.action.tutorials"],
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .offset (-32.dp)

                        ) {
                            val preferences = LocalPreferences.current
                            Checkbox(
                                checked = preferences["welcome.four.show"]?.equals("true") ?: false,
                                onCheckedChange = {
                                    preferences.setProperty("welcome.four.show",it.toString())
                                },
                                modifier = Modifier
                                    .size(24.dp)
                            )
                            Text(
                                text = locale["welcome.action.startup"],
                            )
                        }
                        PDEButton(onClick = { println("Open") }) {
                            val locale = LocalLocale.current
                            Text(locale["welcome.action.go"])
                        }
                    }
                }
            }
        }

        @Composable
        fun intro(){
            val locale = LocalLocale.current
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = locale["welcome.intro.title"],
                        style = typography.h4,
                    )
                    Text(
                        text = locale["welcome.intro.message"],
                        style = typography.body1,
                    )
                }
                Column {
                    Text(
                        text = locale["welcome.intro.suggestion"],
                        style = typography.body1,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primary)
                            .padding(16.dp)
                            .sizeIn(maxWidth = 200.dp)

                    )
                    Image(
                        painter = painterResource("welcome/intro/bubble.svg"),
                        contentDescription = locale["welcome.intro.long"],
                        modifier = Modifier
                            .align{ _, space, _ -> space / 4 }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
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
                        )
                    }
                }
            }
        }
        @Composable
        fun examples(){
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ){
                items(4){
                    Column {
                        Box(
                            modifier = Modifier
                                .background(colors.primary)
                                .width(185.dp)
                                .aspectRatio(16f / 9f)
                        )
                        Text("Example $it")
                    }
                }

            }
        }

        @Composable
        fun chip(content: @Composable () -> Unit){
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.primary, RoundedCornerShape(12.dp))
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            ){
                content()
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            pdeapplication("menu.help.welcome") {
                welcome()
            }
        }
    }
}