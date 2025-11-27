package processing.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import processing.app.Platform
import processing.app.ui.theme.LocalLocale
import processing.app.ui.theme.ProcessingTheme
import javax.swing.JComponent


fun addSurveyToWelcomeScreen(): JComponent {
    return ComposePanel().apply {
        setContent {
            ProcessingTheme {
                val locale = LocalLocale.current
                Box {
                    Row(
                        modifier = Modifier
                            .width(420.dp)
                            .padding(16.dp)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colors.surface)
                            .clickable {
                                Platform.openURL("https://survey.processing.org/")
                            }
                            .pointerHoverIcon(
                                PointerIcon.Hand
                            )
                    ) {
                        Image(
                            painter = painterResource("bird.svg"),
                            contentDescription = locale["beta.logo"],
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(20.dp)
                                .size(50.dp)

                        )
                        Column(
                            modifier = Modifier.padding(12.dp),
                        ) {
                            Text(
                                text = locale["welcome.survey.title"],
                                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = locale["welcome.survey.description"],
                            )

                        }
                    }
                }
            }
        }
    }
}