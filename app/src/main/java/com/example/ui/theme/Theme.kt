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

private val VividColorScheme = darkColorScheme(
  primary = VividPrimary,
  onPrimary = VividOnPrimary,
  secondary = VividSecondary,
  onSecondary = VividText,
  tertiary = VividTertiary,
  background = VividBackground,
  onBackground = VividText,
  surface = VividSurface,
  onSurface = VividText,
  surfaceVariant = VividSurface,
  onSurfaceVariant = VividTextSecondary,
  error = ErrorRed
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark Theme for Premium Vibrant look
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = VividColorScheme, typography = Typography, content = content)
}
