package com.kitchenkeeper.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.kitchenkeeper.data.AccentColor

private val White = Color.White
private val Black = Color.Black

/** Builds a light scheme whose primary/secondary/tertiary roles all derive from [seed]. */
private fun lightSchemeFor(seed: Color): ColorScheme {
    val primary = lerp(seed, Black, 0.1f)
    val surface = lerp(White, seed, 0.03f)
    return lightColorScheme(
        primary = primary,
        onPrimary = White,
        primaryContainer = lerp(seed, White, 0.8f),
        onPrimaryContainer = lerp(seed, Black, 0.65f),
        secondary = lerp(primary, Color.Gray, 0.4f),
        onSecondary = White,
        secondaryContainer = lerp(seed, White, 0.86f),
        onSecondaryContainer = lerp(seed, Black, 0.7f),
        tertiary = lerp(primary, Color.Gray, 0.25f),
        tertiaryContainer = lerp(seed, White, 0.75f),
        background = surface,
        surface = surface,
        surfaceContainer = lerp(White, seed, 0.07f),
        surfaceContainerLow = lerp(White, seed, 0.05f),
        surfaceContainerHigh = lerp(White, seed, 0.1f),
        surfaceContainerHighest = lerp(White, seed, 0.13f),
    )
}

/** Builds a dark scheme whose primary/secondary/tertiary roles all derive from [seed]. */
private fun darkSchemeFor(seed: Color): ColorScheme {
    val primary = lerp(seed, White, 0.45f)
    val surface = lerp(Color(0xFF121212), seed, 0.04f)
    return darkColorScheme(
        primary = primary,
        onPrimary = lerp(seed, Black, 0.7f),
        primaryContainer = lerp(seed, Black, 0.45f),
        onPrimaryContainer = lerp(seed, White, 0.8f),
        secondary = lerp(primary, Color.LightGray, 0.4f),
        onSecondary = lerp(seed, Black, 0.75f),
        secondaryContainer = lerp(seed, Black, 0.6f),
        onSecondaryContainer = lerp(seed, White, 0.8f),
        tertiary = lerp(primary, Color.LightGray, 0.25f),
        tertiaryContainer = lerp(seed, Black, 0.5f),
        background = surface,
        surface = surface,
        surfaceContainer = lerp(Color(0xFF1E1E1E), seed, 0.06f),
        surfaceContainerLow = lerp(Color(0xFF1A1A1A), seed, 0.05f),
        surfaceContainerHigh = lerp(Color(0xFF262626), seed, 0.07f),
        surfaceContainerHighest = lerp(Color(0xFF303030), seed, 0.08f),
    )
}

@Composable
fun KitchenKeeperTheme(
    darkTheme: Boolean,
    accent: AccentColor,
    content: @Composable () -> Unit,
) {
    val seed = Color(accent.seed)
    val colorScheme = if (darkTheme) darkSchemeFor(seed) else lightSchemeFor(seed)
    MaterialTheme(colorScheme = colorScheme, content = content)
}
