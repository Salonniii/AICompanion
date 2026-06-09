package com.example.aicompanion

import androidx.compose.ui.graphics.Color

val BackgroundColor
    get() =
        if (ThemeManager.isDarkTheme.value)
            Color(0xFF0F0F0F)
        else
            Color(0xFFF5F5F5)

val CardColor
    get() =
        if (ThemeManager.isDarkTheme.value)
            Color(0xFF1A1A1A)
        else
            Color.White

val TextColor
    get() =
        if (ThemeManager.isDarkTheme.value)
            Color.White
        else
            Color.Black