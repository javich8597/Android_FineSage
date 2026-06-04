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

private val SophisticatedDarkColorScheme = darkColorScheme(
  primary = SophisticatedPrimary,
  onPrimary = SophisticatedOnPrimary,
  secondary = SophisticatedTertiary,
  tertiary = SophisticatedTertiary,
  background = SophisticatedBackground,
  onBackground = SophisticatedText,
  surface = SophisticatedSurface,
  onSurface = SophisticatedText,
  surfaceVariant = SophisticatedSurfaceVariant,
  onSurfaceVariant = SophisticatedText
)

private val SophisticatedLightColorScheme = lightColorScheme(
  primary = Color(0xFF005AC1),
  onPrimary = Color.White,
  secondary = Color(0xFF535F70),
  tertiary = Color(0xFF6B5778),
  background = Color(0xFFFDFCFF),
  onBackground = Color(0xFF1A1C1E),
  surface = Color(0xFFFDFCFF),
  onSurface = Color(0xFF1A1C1E)
)

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamicColor to respect the tailored premium brand identity exactly
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      darkTheme -> SophisticatedDarkColorScheme
      else -> SophisticatedLightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
