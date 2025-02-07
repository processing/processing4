package processing.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
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
                        Text(
                            text = locale["welcome.action.examples"],
                        )
                        Text(
                            text = locale["welcome.action.tutorials"],
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = locale["welcome.action.startup"],
                        )
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
                            .padding(vertical = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primary)
                            .padding(16.dp)
                            .sizeIn(maxWidth = 200.dp)

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

        @JvmStatic
        fun main(args: Array<String>) {
            pdeapplication("menu.help.welcome") {
                welcome()
            }
        }
    }
}