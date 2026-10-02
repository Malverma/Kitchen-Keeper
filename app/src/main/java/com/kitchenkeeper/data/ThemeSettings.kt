package com.kitchenkeeper.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
}

/** The 12 primary colors the user can pick from; [seed] is the base color the scheme is built around. */
enum class AccentColor(val label: String, val seed: Long) {
    GREEN("Green", 0xFF3B6939),
    TEAL("Teal", 0xFF00796B),
    CYAN("Cyan", 0xFF00838F),
    BLUE("Blue", 0xFF1E63C6),
    INDIGO("Indigo", 0xFF3F51B5),
    PURPLE("Purple", 0xFF7B3FB0),
    PINK("Pink", 0xFFC2185B),
    RED("Red", 0xFFC62828),
    ORANGE("Orange", 0xFFE0661A),
    AMBER("Amber", 0xFFB7860B),
    BROWN("Brown", 0xFF795548),
    SLATE("Slate", 0xFF546E7A),
}

data class ThemeSettings(
    val mode: ThemeMode = ThemeMode.LIGHT,
    val accent: AccentColor = AccentColor.GREEN,
)

/** Persists the user's theme choice in SharedPreferences and exposes it as a flow. */
class ThemeSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("theme_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(
        ThemeSettings(
            mode = enumOrDefault(prefs.getString(KEY_MODE, null), ThemeMode.LIGHT),
            accent = enumOrDefault(prefs.getString(KEY_ACCENT, null), AccentColor.GREEN),
        ),
    )
    val settings: StateFlow<ThemeSettings> = _settings.asStateFlow()

    fun setMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        _settings.value = _settings.value.copy(mode = mode)
    }

    fun setAccent(accent: AccentColor) {
        prefs.edit().putString(KEY_ACCENT, accent.name).apply()
        _settings.value = _settings.value.copy(accent = accent)
    }

    private companion object {
        const val KEY_MODE = "mode"
        const val KEY_ACCENT = "accent"

        inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
            enumValues<T>().firstOrNull { it.name == name } ?: default
    }
}
