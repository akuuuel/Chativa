package com.chativa.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// CHATIVA Custom Brand Colors
val ChativaPurplePrimary = Color(0xFF673AB7)
val ChativaPurpleDark = Color(0xFF512DA8)
val ChativaBlueSecondary = Color(0xFF0288D1)
val ChativaBlueLight = Color(0xFF03A9F4)
val ChativaAccent = Color(0xFF00BCD4)

val ChativaBackgroundLight = Color(0xFFF8F9FA)
val ChativaSurfaceLight = Color(0xFFFFFFFF)
val ChativaBackgroundDark = Color(0xFF121212)
val ChativaSurfaceDark = Color(0xFF1E1E1E)

val ReadTickBlue = Color(0xFF2196F3)
val SentTickGray = Color(0xFF9E9E9E)

fun parseHexColor(hex: String, defaultColor: Color = ChativaPurplePrimary): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        val colorInt = android.graphics.Color.parseColor("#$cleaned")
        Color(colorInt)
    } catch (_: Exception) {
        defaultColor
    }
}
