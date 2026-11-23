package com.omu.japan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.omu.japan.model.AppSettings
import com.omu.japan.model.ThemeMode

data class OmuColors(
    val bg: Color, val bg2: Color, val bg3: Color,
    val text: Color, val muted: Color, val accent: Color,
    val glass: Color, val glassStrong: Color, val stroke: Color,
    val isDark: Boolean
)

val DarkOmuColors = OmuColors(Midnight, Charcoal, DeepNavy, White, Muted, Crimson, GlassWhite, GlassStrong, GlassStroke, true)
val LightOmuColors = OmuColors(LightBg, Color(0xFFE8EDF5), Color(0xFFF8FAFC), LightText, LightMuted, Crimson, Color(0x99FFFFFF), Color(0xCCFFFFFF), Color(0x33000000), false)

val LocalOmuColors = staticCompositionLocalOf { DarkOmuColors }
val LocalOmuSettings = staticCompositionLocalOf { AppSettings() }

private val DarkScheme = darkColorScheme(primary = Crimson, secondary = Crimson, background = Midnight, surface = Charcoal, onPrimary = White, onBackground = White, onSurface = White)
private val LightScheme = lightColorScheme(primary = Crimson, secondary = Crimson, background = LightBg, surface = Color.White, onPrimary = White, onBackground = LightText, onSurface = LightText)

@Composable
fun OmuJapanTheme(settings: AppSettings, content: @Composable () -> Unit) {
    val dark = settings.themeMode == ThemeMode.DARK
    val colors = if (dark) DarkOmuColors else LightOmuColors
    CompositionLocalProvider(LocalOmuColors provides colors, LocalOmuSettings provides settings) {
        MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, typography = OmuTypography, content = content)
    }
}

object OmuTheme {
    val c: OmuColors @Composable get() = LocalOmuColors.current
    val settings: AppSettings @Composable get() = LocalOmuSettings.current
}
