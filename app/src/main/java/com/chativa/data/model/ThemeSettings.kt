package com.chativa.data.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val primaryColorHex: String = "#673AB7", // CHATIVA Purple default
    val secondaryColorHex: String = "#0288D1", // CHATIVA Blue default
    val chatBubbleColorHex: String = "#E8EAF6", // Light Indigo/Purple Tint default
    val chatWallpaper: String = "default",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)
