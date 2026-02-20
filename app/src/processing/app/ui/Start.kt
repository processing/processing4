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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import processing.app.Base
import java.awt.AWTEvent
import java.awt.event.WindowEvent


/**
 * Show a splash screen window. A rewrite of Splash.java
 */
class Start {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val duration = 200
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
                    var launched by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        Toolkit.setIcon(window)
                        visible = true
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
                        ),
                    ) {
                        LaunchedEffect(visible, transition.currentState) {
                            if (launched) return@LaunchedEffect
                            if (!visible) return@LaunchedEffect
                            // Wait until the view is no longer transitioning
                            if (transition.targetState != transition.currentState) return@LaunchedEffect
                            launched = true
                            Base.main(args)
                            // List for any new windows opening, and close the splash when one does
                            java.awt.Toolkit.getDefaultToolkit()
                                .addAWTEventListener({ event ->
                                    if (event.id != WindowEvent.WINDOW_OPENED) return@addAWTEventListener

                                    starting = false
                                }, AWTEvent.WINDOW_EVENT_MASK);
                        }
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