package processing.app.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

class ProcessingColors{
    companion object{
        val blue = Color(0xFF0251c8)
        val lightBlue = Color(0xFF82AFFF)

        val deepBlue = Color(0xFF1e32aa)
        val darkBlue = Color(0xFF0F195A)

        val white = Color(0xFFFFFFFF)
        val lightGray = Color(0xFFF5F5F5)
        val gray = Color(0xFFDBDBDB)
        val darkGray = Color(0xFF898989)
        val darkerGray = Color(0xFF727070)
        val veryDarkGray = Color(0xFF1E1E1E)
        val black = Color(0xFF0D0D0D)

        val error = Color(0xFFFF5757)
        val errorContainer = Color(0xFFFFA6A6)

        val p5Light = Color(0xFFfd9db9)
        val p5Mid = Color(0xFFff4077)
        val p5Dark = Color(0xFFaf1f42)

        val foundationLight = Color(0xFFd4b2fe)
        val foundationMid = Color(0xFF9c4bff)
        val foundationDark = Color(0xFF5501a4)

        val downloadInactive = Color(0xFF8890B3)
        val downloadBackgroundActive = Color(0xFF14508B)
    }
}

val PDELightColor = lightColorScheme(
    primary =  ProcessingColors.blue,
    onPrimary = ProcessingColors.white,

    primaryContainer = ProcessingColors.downloadBackgroundActive,
    onPrimaryContainer = ProcessingColors.darkBlue,

    secondary = ProcessingColors.deepBlue,
    onSecondary = ProcessingColors.white,

    secondaryContainer = ProcessingColors.downloadInactive,
    onSecondaryContainer = ProcessingColors.white,

    tertiary = ProcessingColors.p5Mid,
    onTertiary = ProcessingColors.white,

    tertiaryContainer = ProcessingColors.p5Light,
    onTertiaryContainer = ProcessingColors.p5Dark,

    background = ProcessingColors.white,
    onBackground = ProcessingColors.darkBlue,

    surface = ProcessingColors.lightGray,
    onSurface = ProcessingColors.darkerGray,

    error = ProcessingColors.error,
    onError = ProcessingColors.white,

    errorContainer = ProcessingColors.errorContainer,
    onErrorContainer = ProcessingColors.white
)

val PDEDarkColor = darkColorScheme(
    primary =  ProcessingColors.deepBlue,
    onPrimary = ProcessingColors.white,

    secondary = ProcessingColors.lightBlue,
    onSecondary = ProcessingColors.white,

    tertiary = ProcessingColors.blue,
    onTertiary = ProcessingColors.white,

    background = ProcessingColors.veryDarkGray,
    onBackground = ProcessingColors.white,

    surface = ProcessingColors.darkerGray,
    onSurface = ProcessingColors.lightGray,

    error = ProcessingColors.error,
    onError = ProcessingColors.white,
)