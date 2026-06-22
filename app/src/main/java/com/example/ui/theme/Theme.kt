package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val MinimalistDarkColorScheme = darkColorScheme(
    primary = TealAccent,
    onPrimary = DeepBlack,
    secondary = GlowingPurple,
    onSecondary = DeepBlack,
    background = MidnightBlue,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = NeutralText,
    error = CoralRed,
    onError = Color.White,
    errorContainer = SoftRed,
    onErrorContainer = DeepBlack
)

private val MinimalistLightColorScheme = lightColorScheme(
    primary = BluePrimaryLight,
    onPrimary = Color.White,
    secondary = CleanGreen,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = DeepBlack,
    surface = LightSurface,
    onSurface = DeepBlack,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = NeutralText,
    error = CleanRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) MinimalistDarkColorScheme else MinimalistLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
