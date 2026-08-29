package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

enum class AppThemeMode(val title: String) {
    SYSTEM("پیش‌فرض سیستم"),
    LIGHT("روشن (روز)"),
    DARK("تیره (شب)")
}

enum class AppFontScale(val title: String, val scale: Float) {
    COMPACT("فشرده (استاندارد)", 1.0f),
    MEDIUM("متوسط (خوانا)", 1.12f),
    LARGE("درشت (صنعتی)", 1.25f)
}

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    fontScale: Float = 1.0f,
    persianFont: PersianFontFamilyOption = PersianFontFamilyOption.VAZIRMATN,
    dynamicColor: Boolean = false, // Use our handcrafted industrial theme
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val baseTypography = androidx.compose.runtime.remember(persianFont) {
        getAppTypography(persianFont)
    }

    val scaledTypography = androidx.compose.runtime.remember(fontScale, baseTypography) {
        if (fontScale == 1.0f) baseTypography
        else androidx.compose.material3.Typography(
            displayLarge = baseTypography.displayLarge.copy(fontSize = baseTypography.displayLarge.fontSize * fontScale),
            displayMedium = baseTypography.displayMedium.copy(fontSize = baseTypography.displayMedium.fontSize * fontScale),
            displaySmall = baseTypography.displaySmall.copy(fontSize = baseTypography.displaySmall.fontSize * fontScale),
            headlineLarge = baseTypography.headlineLarge.copy(fontSize = baseTypography.headlineLarge.fontSize * fontScale),
            headlineMedium = baseTypography.headlineMedium.copy(fontSize = baseTypography.headlineMedium.fontSize * fontScale),
            headlineSmall = baseTypography.headlineSmall.copy(fontSize = baseTypography.headlineSmall.fontSize * fontScale),
            titleLarge = baseTypography.titleLarge.copy(fontSize = baseTypography.titleLarge.fontSize * fontScale),
            titleMedium = baseTypography.titleMedium.copy(fontSize = baseTypography.titleMedium.fontSize * fontScale),
            titleSmall = baseTypography.titleSmall.copy(fontSize = baseTypography.titleSmall.fontSize * fontScale),
            bodyLarge = baseTypography.bodyLarge.copy(fontSize = baseTypography.bodyLarge.fontSize * fontScale),
            bodyMedium = baseTypography.bodyMedium.copy(fontSize = baseTypography.bodyMedium.fontSize * fontScale),
            bodySmall = baseTypography.bodySmall.copy(fontSize = baseTypography.bodySmall.fontSize * fontScale),
            labelLarge = baseTypography.labelLarge.copy(fontSize = baseTypography.labelLarge.fontSize * fontScale),
            labelMedium = baseTypography.labelMedium.copy(fontSize = baseTypography.labelMedium.fontSize * fontScale),
            labelSmall = baseTypography.labelSmall.copy(fontSize = baseTypography.labelSmall.fontSize * fontScale)
        )
    }

    MaterialTheme(colorScheme = colorScheme, typography = scaledTypography, content = content)
}

