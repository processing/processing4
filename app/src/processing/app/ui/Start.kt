package processing.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import processing.app.Base

/**
 * Show a splash screen window. A rewrite of Splash.java
 */
class Start {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val duration = 200
            val timeMargin = 50

            application {
                var starting by remember { mutableStateOf(true) }
                Window(
                    visible = starting,
                    onCloseRequest = {  },
                    undecorated = true,
                    transparent = true,
                    resizable = false,
                    state = rememberWindowState(
                        position = WindowPosition(Alignment.Center),
                        width = 578.dp,
                        height = 665.dp
                    )
                ) {
                    var visible by remember { mutableStateOf(false) }
                    val composition = rememberCoroutineScope()
                    LaunchedEffect(Unit) {
                        visible = true
                        composition.launch {
                            delay(duration.toLong() + timeMargin)
                            try {
                                Base.main(args)
                            } catch (e: Exception) {
                                throw InternalError("Failed to invoke main method", e)
                            }
                            composition.launch {
                                visible = false
                                delay(duration.toLong() + timeMargin)
                                starting = false
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = duration,
                                easing = LinearEasing
                            )
                        ),
                        exit = fadeOut(
                            animationSpec = tween(
                                durationMillis = duration,
                                easing = LinearEasing
                            )
                        )
                    ) {
                        Image(
                            painter = painterResource("about-processing.svg"),
                            contentDescription = "About",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }

            }
        }
    }
}