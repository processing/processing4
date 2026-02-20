package processing.app.ui.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import processing.app.LocalPreferences
import processing.app.ui.PDEPreference
import processing.app.ui.PDEPreferencePane
import processing.app.ui.PDEPreferences
import processing.app.ui.preferences.Coding.Companion.coding
import java.awt.GraphicsEnvironment
import javax.swing.JColorChooser

class Sketches {
    companion object {
        val sketches = PDEPreferencePane(
            nameKey = "preferences.pane.sketches",
            icon = { Icon(Select_window, contentDescription = null) },
            after = coding,
        )

        fun register() {
            PDEPreferences.register(
                PDEPreference(
                    key = "run.display",
                    descriptionKey = "preferences.run_sketches_on_display",
                    pane = sketches,
                    control = { preference, setPreference ->
                        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
                        val defaultDevice = ge.defaultScreenDevice
                        val devices = ge.screenDevices

                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            devices.toList().chunked(2).forEach { devices ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    devices.forEachIndexed { index, device ->
                                        val displayNum = (index + 1).toString()
                                        OutlinedButton(
                                            colors = if (preference == displayNum || (device == defaultDevice && preference == "-1")) {
                                                ButtonDefaults.buttonColors()
                                            } else {
                                                ButtonDefaults.outlinedButtonColors()
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            onClick = {
                                                setPreference(if (device == defaultDevice) "-1" else displayNum)
                                            }
                                        ) {

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Box {
                                                    Icon(
                                                        Icons.Default.Monitor,
                                                        modifier = Modifier.size(32.dp),
                                                        contentDescription = null
                                                    )
                                                    Text(
                                                        text = displayNum,
                                                        modifier = Modifier
                                                            .align(Alignment.Center)
                                                            .offset(0.dp, (-2).dp),
                                                        style = MaterialTheme.typography.bodySmall,
                                                    )
                                                }
                                                Text(
                                                    text = "${device.displayMode.width} x ${device.displayMode.height}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                                if (device == defaultDevice) {
                                                    Text(
                                                        text = "Default",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = LocalContentColor.current.copy(0.5f),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                ),
                PDEPreference(
                    key = "run.options.memory",
                    descriptionKey = "preferences.increase_memory",
                    pane = sketches,
                    control = { preference, setPreference ->
                        Switch(
                            checked = preference?.toBoolean() ?: false,
                            onCheckedChange = {
                                setPreference(it.toString())
                            }
                        )
                    }
                ),
                PDEPreference(
                    key = "run.options.memory.maximum",
                    descriptionKey = "preferences.increase_max_memory",
                    pane = sketches,
                    control = { preference, setPreference ->
                        OutlinedTextField(
                            enabled = LocalPreferences.current["run.options.memory"]?.toBoolean() ?: false,
                            modifier = Modifier.widthIn(max = 300.dp),
                            value = preference ?: "",
                            singleLine = true,
                            trailingIcon = { Text("MB") },
                            onValueChange = {
                                setPreference(it)
                            }
                        )
                    }
                ),
                PDEPreference(
                    key = "run.present.bgcolor",
                    descriptionKey = "preferences.background_color",
                    pane = sketches,
                    control = { preference, setPreference ->
                        val color = try {
                            java.awt.Color.decode(preference)
                        } catch (e: Exception) {
                            java.awt.Color.BLACK
                        }
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .padding(4.dp)
                                .background(
                                    color = Color(color.red, color.green, color.blue),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    // TODO: Replace with Compose color picker when available
                                    val newColor = JColorChooser.showDialog(
                                        null,
                                        "Choose Background Color",
                                        color
                                    ) ?: color
                                    val hexColor =
                                        String.format("#%02x%02x%02x", newColor.red, newColor.green, newColor.blue)
                                    setPreference(hexColor)
                                }
                        )
                    }
                )
            )
        }
        val Select_window: ImageVector
            get() {
                if (_Select_window != null) return _Select_window!!

                _Select_window = ImageVector.Builder(
                    name = "Select_window",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 960f,
                    viewportHeight = 960f
                ).apply {
                    path(
                        fill = SolidColor(Color(0xFF000000))
                    ) {
                        moveTo(160f, 880f)
                        quadToRelative(-33f, 0f, -56.5f, -23.5f)
                        reflectiveQuadTo(80f, 800f)
                        verticalLineToRelative(-360f)
                        quadToRelative(0f, -33f, 23.5f, -56.5f)
                        reflectiveQuadTo(160f, 360f)
                        horizontalLineToRelative(80f)
                        verticalLineToRelative(-200f)
                        quadToRelative(0f, -33f, 23.5f, -56.5f)
                        reflectiveQuadTo(320f, 80f)
                        horizontalLineToRelative(480f)
                        quadToRelative(33f, 0f, 56.5f, 23.5f)
                        reflectiveQuadTo(880f, 160f)
                        verticalLineToRelative(360f)
                        quadToRelative(0f, 33f, -23.5f, 56.5f)
                        reflectiveQuadTo(800f, 600f)
                        horizontalLineToRelative(-80f)
                        verticalLineToRelative(200f)
                        quadToRelative(0f, 33f, -23.5f, 56.5f)
                        reflectiveQuadTo(640f, 880f)
                        close()
                        moveToRelative(0f, -80f)
                        horizontalLineToRelative(480f)
                        verticalLineToRelative(-280f)
                        horizontalLineTo(160f)
                        close()
                        moveToRelative(560f, -280f)
                        horizontalLineToRelative(80f)
                        verticalLineToRelative(-280f)
                        horizontalLineTo(320f)
                        verticalLineToRelative(120f)
                        horizontalLineToRelative(320f)
                        quadToRelative(33f, 0f, 56.5f, 23.5f)
                        reflectiveQuadTo(720f, 440f)
                        close()
                    }
                }.build()

                return _Select_window!!
            }

        private var _Select_window: ImageVector? = null
    }
}