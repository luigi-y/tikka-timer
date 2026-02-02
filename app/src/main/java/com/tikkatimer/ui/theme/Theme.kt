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
    )
private val DefaultLightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
    )

// Ocean (Blue)
private val OceanDarkColorScheme =
    darkColorScheme(
        primary = Ocean80,
        secondary = OceanGrey80,
        tertiary = OceanAccent80,
    )
private val OceanLightColorScheme =
    lightColorScheme(
        primary = Ocean40,
        secondary = OceanGrey40,
        tertiary = OceanAccent40,
    )

// Forest (Green)
private val ForestDarkColorScheme =
    darkColorScheme(
        primary = Forest80,
        secondary = ForestGrey80,
        tertiary = ForestAccent80,
    )
private val ForestLightColorScheme =
    lightColorScheme(
        primary = Forest40,
        secondary = ForestGrey40,
        tertiary = ForestAccent40,
    )

// Sunset (Orange)
private val SunsetDarkColorScheme =
    darkColorScheme(
        primary = Sunset80,
        secondary = SunsetGrey80,
        tertiary = SunsetAccent80,
    )
private val SunsetLightColorScheme =
    lightColorScheme(
        primary = Sunset40,
        secondary = SunsetGrey40,
        tertiary = SunsetAccent40,
    )

// Cherry (Red/Pink)
private val CherryDarkColorScheme =
    darkColorScheme(
        primary = Cherry80,
        secondary = CherryGrey80,
        tertiary = CherryAccent80,
    )
private val CherryLightColorScheme =
    lightColorScheme(
        primary = Cherry40,
        secondary = CherryGrey40,
        tertiary = CherryAccent40,
    )

// Lavender (Light Purple)
private val LavenderDarkColorScheme =
    darkColorScheme(
        primary = Lavender80,
        secondary = LavenderGrey80,
        tertiary = LavenderAccent80,
    )
private val LavenderLightColorScheme =
    lightColorScheme(
        primary = Lavender40,
        secondary = LavenderGrey40,
        tertiary = LavenderAccent40,
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
