package com.aktarjabed.nextgenkeyboard.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KeyboardTheme(
    val id: String,
    val name: String,
    val keyBackgroundColor: Color,
    val keyTextColor: Color,
    val primaryAccentColor: Color,
    val secondaryAccentColor: Color,
    val backgroundColor: Color,
    val borderColor: Color,
    val shadowElevation: Dp,
    val keyCornerRadius: Dp,
    val keyPadding: Dp,
    val keyElevation: Dp,
    val isDarkMode: Boolean
)

object KeyboardThemes {
    val LIGHT = KeyboardTheme(
        id = "light",
        name = "Light",
        keyBackgroundColor = Color(0xFFFFFFFF),
        keyTextColor = Color(0xFF000000),
        primaryAccentColor = Color(0xFF2196F3),
        secondaryAccentColor = Color(0xFFF1F3F5),
        backgroundColor = Color(0xFFF5F5F5),
        borderColor = Color(0xFFE0E0E0),
        shadowElevation = 2.dp,
        keyCornerRadius = 8.dp,
        keyPadding = 8.dp,
        keyElevation = 4.dp,
        isDarkMode = false
    )

    val DARK = KeyboardTheme(
        id = "dark",
        name = "Dark",
        keyBackgroundColor = Color(0xFF2C2C2C),
        keyTextColor = Color(0xFFFFFFFF),
        primaryAccentColor = Color(0xFF64B5F6),
        secondaryAccentColor = Color(0xFF424242),
        backgroundColor = Color(0xFF1A1A1A),
        borderColor = Color(0xFF3F3F3F),
        shadowElevation = 2.dp,
        keyCornerRadius = 8.dp,
        keyPadding = 8.dp,
        keyElevation = 4.dp,
        isDarkMode = true
    )

    val NEON = KeyboardTheme(
        id = "neon",
        name = "Neon",
        keyBackgroundColor = Color(0xFF0A0E27),
        keyTextColor = Color(0xFF00FF88),
        primaryAccentColor = Color(0xFFFF00FF),
        secondaryAccentColor = Color(0xFF00FFFF),
        backgroundColor = Color(0xFF000000),
        borderColor = Color(0xFF00FF88),
        shadowElevation = 8.dp,
        keyCornerRadius = 4.dp,
        keyPadding = 6.dp,
        keyElevation = 8.dp,
        isDarkMode = true
    )

    val GLASS_MORPHISM = KeyboardTheme(
        id = "glass",
        name = "Glass",
        keyBackgroundColor = Color(0xFFFFFFFF).copy(alpha = 0.25f),
        keyTextColor = Color(0xFFFFFFFF),
        primaryAccentColor = Color(0xFF64B5F6),
        secondaryAccentColor = Color(0xFFFFFFFF).copy(alpha = 0.15f),
        backgroundColor = Color(0xFF1A1A2E).copy(alpha = 0.9f),
        borderColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
        shadowElevation = 0.dp,
        keyCornerRadius = 16.dp,
        keyPadding = 10.dp,
        keyElevation = 0.dp,
        isDarkMode = true
    )

    val MATERIAL_YOU = KeyboardTheme(
        id = "material_you",
        name = "Material You",
        keyBackgroundColor = Color(0xFFE7E0EC),
        keyTextColor = Color(0xFF1C1B1F),
        primaryAccentColor = Color(0xFF6750A4),
        secondaryAccentColor = Color(0xFFEADDFF),
        backgroundColor = Color(0xFFFFFBFE),
        borderColor = Color(0xFFE0E0E0),
        shadowElevation = 2.dp,
        keyCornerRadius = 12.dp,
        keyPadding = 8.dp,
        keyElevation = 0.dp,
        isDarkMode = false
    )

    val GAMING = KeyboardTheme(
        id = "gaming",
        name = "Gaming RGB",
        keyBackgroundColor = Color(0xFF1A1A2E),
        keyTextColor = Color(0xFFFF00FF),
        primaryAccentColor = Color(0xFF00FF00),
        secondaryAccentColor = Color(0xFFFF0080),
        backgroundColor = Color(0xFF0F0F1E),
        borderColor = Color(0xFF00FF00),
        shadowElevation = 6.dp,
        keyCornerRadius = 6.dp,
        keyPadding = 6.dp,
        keyElevation = 6.dp,
        isDarkMode = true
    )

    val ALL_THEMES = listOf(LIGHT, DARK, NEON, GLASS_MORPHISM, MATERIAL_YOU, GAMING)
}
