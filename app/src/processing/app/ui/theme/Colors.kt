package processing.app.ui.theme

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color

val PDELightColors = Colors(
    primary = Color("#0F195A".toColorInt()),
    primaryVariant = Color("#1F34AB".toColorInt()),
    secondary = Color("#82AFFF".toColorInt()),
    secondaryVariant = Color("#0468FF".toColorInt()),
    background = Color("#FFFFFF".toColorInt()),
    surface = Color("#C0D7FF".toColorInt()),
    error = Color("#0F195A".toColorInt()),
    onPrimary = Color("#FFFFFF".toColorInt()),
    onSecondary = Color("#FFFFFF".toColorInt()),
    onBackground = Color("#0F195A".toColorInt()),
    onSurface = Color("#FFFFFF".toColorInt()),
    onError = Color("#0F195A".toColorInt()),
    isLight = true,
)

fun String.toColorInt(): Int {
    if (this[0] == '#') {
        var color = substring(1).toLong(16)
        if (length == 7) {
            color = color or 0x00000000ff000000L
        } else if (length != 9) {
            throw IllegalArgumentException("Unknown color")
        }
        return color.toInt()
    }
    throw IllegalArgumentException("Unknown color")
}