package by.dreb.tutorhelper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LavenderDarkPrimary,
    secondary = LavenderDarkSecondary,
    tertiary = LavenderDarkAccent,
    background = Color(0xFF15121C),
    surface = Color(0xFF1E1A26),
    onPrimary = WhiteBase,
    onSecondary = WhiteBase,
    onTertiary = WhiteBase,
    onBackground = WhiteBase,
    onSurface = WhiteBase
)

private val LightColorScheme = lightColorScheme(
    primary = LavenderPrimary,
    secondary = LavenderSecondary,
    tertiary = LavenderAccent,
    background = WhiteBase,
    surface = LavenderSurface,
    onPrimary = WhiteBase,
    onSecondary = Color(0xFF2D2540),
    onTertiary = WhiteBase,
    onBackground = Color(0xFF1D1B21),
    onSurface = Color(0xFF1D1B21)
)

@Composable
fun TutorHelperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
