@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.caloriebank.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Bank-book palette: deep teal ink on cream paper, warm amber for credits.
private val Light = lightColorScheme(
    primary = Color(0xFF1B5E4A), onPrimary = Color.White,
    primaryContainer = Color(0xFFA9F2D8), onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF8A5A00), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDEA6), onSecondaryContainer = Color(0xFF2C1A00),
    tertiary = Color(0xFF00658E), tertiaryContainer = Color(0xFFC7E7FF), onTertiaryContainer = Color(0xFF001E2E),
    background = Color(0xFFFBF8F1), surface = Color(0xFFFBF8F1), onSurface = Color(0xFF1C1B17),
    surfaceVariant = Color(0xFFE2E8E1), onSurfaceVariant = Color(0xFF3F4945),
    surfaceContainer = Color(0xFFF1EEE7), surfaceContainerHigh = Color(0xFFEBE8E1), surfaceContainerHighest = Color(0xFFE5E2DB),
    error = Color(0xFFBA1A1A), errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
)
private val Dark = darkColorScheme(
    primary = Color(0xFF8DD6BD), onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF00513C), onPrimaryContainer = Color(0xFFA9F2D8),
    secondary = Color(0xFFF7BD5B), onSecondary = Color(0xFF442B00),
    secondaryContainer = Color(0xFF684000), onSecondaryContainer = Color(0xFFFFDEA6),
    tertiary = Color(0xFF86CEFF), tertiaryContainer = Color(0xFF004C6C), onTertiaryContainer = Color(0xFFC7E7FF),
    background = Color(0xFF13130F), surface = Color(0xFF13130F), onSurface = Color(0xFFE6E2DA),
    surfaceVariant = Color(0xFF3F4945), onSurfaceVariant = Color(0xFFBFC9C3),
    surfaceContainer = Color(0xFF1F1F1B), surfaceContainerHigh = Color(0xFF2A2925), surfaceContainerHighest = Color(0xFF353430),
    error = Color(0xFFFFB4AB), errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
)

val AppTypography = Typography().let { t ->
    t.copy(
        displayLarge = t.displayLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1.5).sp),
        displayMedium = t.displayMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun CalorieBankTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        darkTheme -> Dark
        else -> Light
    }
    MaterialExpressiveTheme(
        colorScheme = scheme,
        motionScheme = MotionScheme.expressive(),
        typography = AppTypography,
        content = content,
    )
}
