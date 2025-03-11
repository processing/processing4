package processing.app.ui.theme

import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp

val processingFont = FontFamily(
    Font(
        resource = "ProcessingSans-Regular.ttf",
        weight = FontWeight.Normal,
        style = FontStyle.Normal
    ),
    Font(
        resource = "ProcessingSans-Bold.ttf",
        weight = FontWeight.Bold,
        style = FontStyle.Normal
    )
)
val spaceGroteskFont = FontFamily(
    Font(
        resource = "SpaceGrotesk-Bold.ttf",
        weight = FontWeight.Bold,
    ),
    Font(
        resource = "SpaceGrotesk-Regular.ttf",
        weight = FontWeight.Normal,
    ),
    Font(
        resource = "SpaceGrotesk-Medium.ttf",
        weight = FontWeight.Medium,
    ),
    Font(
        resource = "SpaceGrotesk-SemiBold.ttf",
        weight = FontWeight.SemiBold,
    ),
    Font(
        resource = "SpaceGrotesk-Light.ttf",
        weight = FontWeight.Light,
    )
)

val Typography = Typography(
    defaultFontFamily = spaceGroteskFont,
    h4 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 24.sp
    ),
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 19.sp
    ),
)