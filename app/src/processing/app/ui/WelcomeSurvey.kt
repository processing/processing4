package processing.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import processing.app.ui.theme.PDETheme
import javax.swing.JComponent

@Composable
fun SurveyInvitation() {
    val locale = LocalLocale.current
    Row(
        modifier = Modifier
            .width(420.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable {
                Platform.openURL("https://survey.processing.org/")
            }
            .pointerHoverIcon(
                PointerIcon.Hand
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
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
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = locale["welcome.survey.description"],
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}