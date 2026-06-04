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
    primary = BrightCyan,
    onPrimary = DeepBlack,
    secondary = SoftGreen,
    onSecondary = DeepBlack,
    background = DeepBlack,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = NeutralText,
    error = SoftRed,
    onError = PureWhite
)

private val MinimalistLightColorScheme = lightColorScheme(
    primary = DarkBlueAccent,
    onPrimary = PureWhite,
    secondary = CleanGreen,
    onSecondary = PureWhite,
    background = PureWhite,
    onBackground = DeepBlack,
    surface = LightSurface,
    onSurface = DeepBlack,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = NeutralText,
    error = CleanRed,
    onError = PureWhite
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
