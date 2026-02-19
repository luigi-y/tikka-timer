package com.tikkatimer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.tikkatimer.domain.model.ColorTheme

// Default (Purple)
private val DefaultDarkColorScheme =
    darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80,
        primaryContainer = DefaultPrimaryContainer80,
        onPrimaryContainer = DefaultOnPrimaryContainer80,
        secondaryContainer = DefaultSecondaryContainer80,
        onSecondaryContainer = DefaultOnSecondaryContainer80,
        surface = DefaultSurface80,
        onSurface = DefaultOnSurface80,
        surfaceVariant = DefaultSurfaceVariant80,
        onSurfaceVariant = DefaultOnSurfaceVariant80,
        background = DefaultBackground80,
        onBackground = DefaultOnBackground80,
        outline = DefaultOutline80,
        outlineVariant = DefaultOutlineVariant80,
    )
private val DefaultLightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
        primaryContainer = DefaultPrimaryContainer40,
        onPrimaryContainer = DefaultOnPrimaryContainer40,
        secondaryContainer = DefaultSecondaryContainer40,
        onSecondaryContainer = DefaultOnSecondaryContainer40,
        surface = DefaultSurface40,
        onSurface = DefaultOnSurface40,
        surfaceVariant = DefaultSurfaceVariant40,
        onSurfaceVariant = DefaultOnSurfaceVariant40,
        background = DefaultBackground40,
        onBackground = DefaultOnBackground40,
        outline = DefaultOutline40,
        outlineVariant = DefaultOutlineVariant40,
    )

// Ocean (Blue)
private val OceanDarkColorScheme =
    darkColorScheme(
        primary = Ocean80,
        secondary = OceanGrey80,
        tertiary = OceanAccent80,
        primaryContainer = OceanPrimaryContainer80,
        onPrimaryContainer = OceanOnPrimaryContainer80,
        secondaryContainer = OceanSecondaryContainer80,
        onSecondaryContainer = OceanOnSecondaryContainer80,
        surface = OceanSurface80,
        onSurface = OceanOnSurface80,
        surfaceVariant = OceanSurfaceVariant80,
        onSurfaceVariant = OceanOnSurfaceVariant80,
        background = OceanBackground80,
        onBackground = OceanOnBackground80,
        outline = OceanOutline80,
        outlineVariant = OceanOutlineVariant80,
    )
private val OceanLightColorScheme =
    lightColorScheme(
        primary = Ocean40,
        secondary = OceanGrey40,
        tertiary = OceanAccent40,
        primaryContainer = OceanPrimaryContainer40,
        onPrimaryContainer = OceanOnPrimaryContainer40,
        secondaryContainer = OceanSecondaryContainer40,
        onSecondaryContainer = OceanOnSecondaryContainer40,
        surface = OceanSurface40,
        onSurface = OceanOnSurface40,
        surfaceVariant = OceanSurfaceVariant40,
        onSurfaceVariant = OceanOnSurfaceVariant40,
        background = OceanBackground40,
        onBackground = OceanOnBackground40,
        outline = OceanOutline40,
        outlineVariant = OceanOutlineVariant40,
    )

// Forest (Green)
private val ForestDarkColorScheme =
    darkColorScheme(
        primary = Forest80,
        secondary = ForestGrey80,
        tertiary = ForestAccent80,
        primaryContainer = ForestPrimaryContainer80,
        onPrimaryContainer = ForestOnPrimaryContainer80,
        secondaryContainer = ForestSecondaryContainer80,
        onSecondaryContainer = ForestOnSecondaryContainer80,
        surface = ForestSurface80,
        onSurface = ForestOnSurface80,
        surfaceVariant = ForestSurfaceVariant80,
        onSurfaceVariant = ForestOnSurfaceVariant80,
        background = ForestBackground80,
        onBackground = ForestOnBackground80,
        outline = ForestOutline80,
        outlineVariant = ForestOutlineVariant80,
    )
private val ForestLightColorScheme =
    lightColorScheme(
        primary = Forest40,
        secondary = ForestGrey40,
        tertiary = ForestAccent40,
        primaryContainer = ForestPrimaryContainer40,
        onPrimaryContainer = ForestOnPrimaryContainer40,
        secondaryContainer = ForestSecondaryContainer40,
        onSecondaryContainer = ForestOnSecondaryContainer40,
        surface = ForestSurface40,
        onSurface = ForestOnSurface40,
        surfaceVariant = ForestSurfaceVariant40,
        onSurfaceVariant = ForestOnSurfaceVariant40,
        background = ForestBackground40,
        onBackground = ForestOnBackground40,
        outline = ForestOutline40,
        outlineVariant = ForestOutlineVariant40,
    )

// Sunset (Orange)
private val SunsetDarkColorScheme =
    darkColorScheme(
        primary = Sunset80,
        secondary = SunsetGrey80,
        tertiary = SunsetAccent80,
        primaryContainer = SunsetPrimaryContainer80,
        onPrimaryContainer = SunsetOnPrimaryContainer80,
        secondaryContainer = SunsetSecondaryContainer80,
        onSecondaryContainer = SunsetOnSecondaryContainer80,
        surface = SunsetSurface80,
        onSurface = SunsetOnSurface80,
        surfaceVariant = SunsetSurfaceVariant80,
        onSurfaceVariant = SunsetOnSurfaceVariant80,
        background = SunsetBackground80,
        onBackground = SunsetOnBackground80,
        outline = SunsetOutline80,
        outlineVariant = SunsetOutlineVariant80,
    )
private val SunsetLightColorScheme =
    lightColorScheme(
        primary = Sunset40,
        secondary = SunsetGrey40,
        tertiary = SunsetAccent40,
        primaryContainer = SunsetPrimaryContainer40,
        onPrimaryContainer = SunsetOnPrimaryContainer40,
        secondaryContainer = SunsetSecondaryContainer40,
        onSecondaryContainer = SunsetOnSecondaryContainer40,
        surface = SunsetSurface40,
        onSurface = SunsetOnSurface40,
        surfaceVariant = SunsetSurfaceVariant40,
        onSurfaceVariant = SunsetOnSurfaceVariant40,
        background = SunsetBackground40,
        onBackground = SunsetOnBackground40,
        outline = SunsetOutline40,
        outlineVariant = SunsetOutlineVariant40,
    )

// Cherry (Red/Pink)
private val CherryDarkColorScheme =
    darkColorScheme(
        primary = Cherry80,
        secondary = CherryGrey80,
        tertiary = CherryAccent80,
        primaryContainer = CherryPrimaryContainer80,
        onPrimaryContainer = CherryOnPrimaryContainer80,
        secondaryContainer = CherrySecondaryContainer80,
        onSecondaryContainer = CherryOnSecondaryContainer80,
        surface = CherrySurface80,
        onSurface = CherryOnSurface80,
        surfaceVariant = CherrySurfaceVariant80,
        onSurfaceVariant = CherryOnSurfaceVariant80,
        background = CherryBackground80,
        onBackground = CherryOnBackground80,
        outline = CherryOutline80,
        outlineVariant = CherryOutlineVariant80,
    )
private val CherryLightColorScheme =
    lightColorScheme(
        primary = Cherry40,
        secondary = CherryGrey40,
        tertiary = CherryAccent40,
        primaryContainer = CherryPrimaryContainer40,
        onPrimaryContainer = CherryOnPrimaryContainer40,
        secondaryContainer = CherrySecondaryContainer40,
        onSecondaryContainer = CherryOnSecondaryContainer40,
        surface = CherrySurface40,
        onSurface = CherryOnSurface40,
        surfaceVariant = CherrySurfaceVariant40,
        onSurfaceVariant = CherryOnSurfaceVariant40,
        background = CherryBackground40,
        onBackground = CherryOnBackground40,
        outline = CherryOutline40,
        outlineVariant = CherryOutlineVariant40,
    )

// Lavender (Light Purple)
private val LavenderDarkColorScheme =
    darkColorScheme(
        primary = Lavender80,
        secondary = LavenderGrey80,
        tertiary = LavenderAccent80,
        primaryContainer = LavenderPrimaryContainer80,
        onPrimaryContainer = LavenderOnPrimaryContainer80,
        secondaryContainer = LavenderSecondaryContainer80,
        onSecondaryContainer = LavenderOnSecondaryContainer80,
        surface = LavenderSurface80,
        onSurface = LavenderOnSurface80,
        surfaceVariant = LavenderSurfaceVariant80,
        onSurfaceVariant = LavenderOnSurfaceVariant80,
        background = LavenderBackground80,
        onBackground = LavenderOnBackground80,
        outline = LavenderOutline80,
        outlineVariant = LavenderOutlineVariant80,
    )
private val LavenderLightColorScheme =
    lightColorScheme(
        primary = Lavender40,
        secondary = LavenderGrey40,
        tertiary = LavenderAccent40,
        primaryContainer = LavenderPrimaryContainer40,
        onPrimaryContainer = LavenderOnPrimaryContainer40,
        secondaryContainer = LavenderSecondaryContainer40,
        onSecondaryContainer = LavenderOnSecondaryContainer40,
        surface = LavenderSurface40,
        onSurface = LavenderOnSurface40,
        surfaceVariant = LavenderSurfaceVariant40,
        onSurfaceVariant = LavenderOnSurfaceVariant40,
        background = LavenderBackground40,
        onBackground = LavenderOnBackground40,
        outline = LavenderOutline40,
        outlineVariant = LavenderOutlineVariant40,
    )

/**
 * Tikka Timer 앱 테마
 * Material Design 3 기반, 다양한 컬러 테마 지원
 */
@Composable
fun TikkaTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorTheme: ColorTheme = ColorTheme.DEFAULT,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when (colorTheme) {
            ColorTheme.DEFAULT -> if (darkTheme) DefaultDarkColorScheme else DefaultLightColorScheme
            ColorTheme.OCEAN -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
            ColorTheme.FOREST -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
            ColorTheme.SUNSET -> if (darkTheme) SunsetDarkColorScheme else SunsetLightColorScheme
            ColorTheme.CHERRY -> if (darkTheme) CherryDarkColorScheme else CherryLightColorScheme
            ColorTheme.LAVENDER -> if (darkTheme) LavenderDarkColorScheme else LavenderLightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
