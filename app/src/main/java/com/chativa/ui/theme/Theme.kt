package com.chativa.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.chativa.data.model.ThemeMode
import com.chativa.data.model.ThemeSettings

val LocalChativaCustomColors = staticCompositionLocalOf {
    ChativaCustomColors(
        chatBubbleColor = Color(0xFFE8EAF6),
        primaryColor = ChativaPurplePrimary,
        secondaryColor = ChativaBlueSecondary
    )
}

data class ChativaCustomColors(
    val chatBubbleColor: Color,
    val primaryColor: Color,
    val secondaryColor: Color
)

@Composable
fun ChativaTheme(
    themeSettings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeSettings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val primaryColor = parseHexColor(themeSettings.primaryColorHex, ChativaPurplePrimary)
    val secondaryColor = parseHexColor(themeSettings.secondaryColorHex, ChativaBlueSecondary)
    val chatBubbleColor = parseHexColor(themeSettings.chatBubbleColorHex, Color(0xFFE8EAF6))

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            background = ChativaBackgroundDark,
            surface = ChativaSurfaceDark,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            background = ChativaBackgroundLight,
            surface = ChativaSurfaceLight,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF1F1F1F),
            onSurface = Color(0xFF1F1F1F)
        )
    }

    val customColors = ChativaCustomColors(
        chatBubbleColor = if (darkTheme) parseHexColor(themeSettings.chatBubbleColorHex, Color(0xFF2C2C3E)) else chatBubbleColor,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor
    )

    CompositionLocalProvider(LocalChativaCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
