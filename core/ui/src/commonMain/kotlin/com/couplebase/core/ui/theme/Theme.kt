package com.couplebase.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Rose50,
    onPrimary = Rose99,
    primaryContainer = Rose90,
    onPrimaryContainer = Rose10,
    secondary = Teal50,
    onSecondary = Teal99,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral30,
    outline = Neutral50,
    outlineVariant = Neutral80,
    error = Error40,
    onError = Neutral99,
    errorContainer = Error90,
    onErrorContainer = Error40,
)

private val DarkColorScheme = darkColorScheme(
    primary = Rose80,
    onPrimary = Rose20,
    primaryContainer = Rose30,
    onPrimaryContainer = Rose90,
    secondary = Teal80,
    onSecondary = Teal20,
    secondaryContainer = Teal30,
    onSecondaryContainer = Teal90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral30,
    onSurfaceVariant = Neutral80,
    outline = Neutral60,
    outlineVariant = Neutral30,
    error = Error80,
    onError = Neutral10,
    errorContainer = Error40,
    onErrorContainer = Error90,
)

@Composable
fun CouplebaseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CouplebaseTypography,
        shapes = CouplebaseShapes,
        content = content,
    )
}
