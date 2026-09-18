@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.caloriebank.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Bank-book palette: deep teal ink on cream paper, warm amber for credits, sky for water.
// A fixed brand palette (not dynamic colour) so the hero gradients and semantic colours hold up.
private val Light = lightColorScheme(
    primary = Color(0xFF0F6B55), onPrimary = Color.White,
    primaryContainer = Color(0xFFA9F2D8), onPrimaryContainer = Color(0xFF00201A),
    inversePrimary = Color(0xFF8DD6BD),
    secondary = Color(0xFF8A5A00), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDEA6), onSecondaryContainer = Color(0xFF2C1A00),
    tertiary = Color(0xFF00658E), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC7E7FF), onTertiaryContainer = Color(0xFF001E2E),
    background = Color(0xFFFAF7F0), onBackground = Color(0xFF1C1B17),
    surface = Color(0xFFFAF7F0), onSurface = Color(0xFF1C1B17),
    surfaceVariant = Color(0xFFE2E8E1), onSurfaceVariant = Color(0xFF3F4945),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF4F1EA),
    surfaceContainer = Color(0xFFEFECE4), surfaceContainerHigh = Color(0xFFE9E6DF), surfaceContainerHighest = Color(0xFFE3E0D9),
    outline = Color(0xFF6F7975), outlineVariant = Color(0xFFBFC9C3),
    error = Color(0xFFBA1A1A), errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
)
private val Dark = darkColorScheme(
    primary = Color(0xFF8DD6BD), onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF00513C), onPrimaryContainer = Color(0xFFA9F2D8),
    inversePrimary = Color(0xFF0F6B55),
    secondary = Color(0xFFF7BD5B), onSecondary = Color(0xFF442B00),
    secondaryContainer = Color(0xFF684000), onSecondaryContainer = Color(0xFFFFDEA6),
    tertiary = Color(0xFF86CEFF), onTertiary = Color(0xFF00344C),
    tertiaryContainer = Color(0xFF004C6C), onTertiaryContainer = Color(0xFFC7E7FF),
    background = Color(0xFF12140F), onBackground = Color(0xFFE6E2DA),
    surface = Color(0xFF12140F), onSurface = Color(0xFFE6E2DA),
    surfaceVariant = Color(0xFF3F4945), onSurfaceVariant = Color(0xFFBFC9C3),
    surfaceContainerLowest = Color(0xFF0D0F0B), surfaceContainerLow = Color(0xFF1A1C17),
    surfaceContainer = Color(0xFF1F211C), surfaceContainerHigh = Color(0xFF292B26), surfaceContainerHighest = Color(0xFF343630),
    outline = Color(0xFF89938E), outlineVariant = Color(0xFF3F4945),
    error = Color(0xFFFFB4AB), errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
)

val AppTypography = Typography().let { t ->
    t.copy(
        displayLarge = t.displayLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-2).sp),
        displayMedium = t.displayMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.5).sp),
        displaySmall = t.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
        headlineLarge = t.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = t.labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp),
    )
}

@Composable
fun CalorieBankTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialExpressiveTheme(
        colorScheme = if (darkTheme) Dark else Light,
        motionScheme = MotionScheme.expressive(),
        typography = AppTypography,
        content = content,
    )
}
