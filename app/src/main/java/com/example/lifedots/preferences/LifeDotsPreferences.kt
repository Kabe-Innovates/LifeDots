package com.example.lifedots.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeOption {
    LIGHT, DARK, AMOLED, CUSTOM
}

enum class DotSize {
    TINY, SMALL, MEDIUM, LARGE, HUGE
}

enum class DotShape {
    CIRCLE, SQUARE, ROUNDED_SQUARE, DIAMOND
}

enum class GridDensity {
    COMPACT, NORMAL, RELAXED, SPACIOUS
}

data class CustomColors(
    val backgroundColor: Int = 0xFF1A1A1A.toInt(),
    val filledDotColor: Int = 0xFFE0E0E0.toInt(),
    val emptyDotColor: Int = 0xFF3A3A3A.toInt(),
    val todayDotColor: Int = 0xFF5BA0E9.toInt()
)

data class WallpaperSettings(
    val theme: ThemeOption = ThemeOption.DARK,
    val dotSize: DotSize = DotSize.MEDIUM,
    val dotShape: DotShape = DotShape.CIRCLE,
    val gridDensity: GridDensity = GridDensity.COMPACT,
    val highlightToday: Boolean = true,
    val filledDotAlpha: Float = 1.0f,
    val emptyDotAlpha: Float = 1.0f,
    val customColors: CustomColors = CustomColors()
)

class LifeDotsPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<WallpaperSettings> = _settingsFlow.asStateFlow()

    val settings: WallpaperSettings
        get() = _settingsFlow.value

    private fun loadSettings(): WallpaperSettings {
        val customColors = CustomColors(
            backgroundColor = prefs.getInt(KEY_CUSTOM_BG_COLOR, 0xFF1A1A1A.toInt()),
            filledDotColor = prefs.getInt(KEY_CUSTOM_FILLED_COLOR, 0xFFE0E0E0.toInt()),
            emptyDotColor = prefs.getInt(KEY_CUSTOM_EMPTY_COLOR, 0xFF3A3A3A.toInt()),
            todayDotColor = prefs.getInt(KEY_CUSTOM_TODAY_COLOR, 0xFF5BA0E9.toInt())
        )

        return WallpaperSettings(
            theme = ThemeOption.valueOf(prefs.getString(KEY_THEME, ThemeOption.DARK.name) ?: ThemeOption.DARK.name),
            dotSize = DotSize.valueOf(prefs.getString(KEY_DOT_SIZE, DotSize.MEDIUM.name) ?: DotSize.MEDIUM.name),
            dotShape = DotShape.valueOf(prefs.getString(KEY_DOT_SHAPE, DotShape.CIRCLE.name) ?: DotShape.CIRCLE.name),
            gridDensity = GridDensity.valueOf(prefs.getString(KEY_GRID_DENSITY, GridDensity.COMPACT.name) ?: GridDensity.COMPACT.name),
            highlightToday = prefs.getBoolean(KEY_HIGHLIGHT_TODAY, true),
            filledDotAlpha = prefs.getFloat(KEY_FILLED_DOT_ALPHA, 1.0f),
            emptyDotAlpha = prefs.getFloat(KEY_EMPTY_DOT_ALPHA, 1.0f),
            customColors = customColors
        )
    }

    fun setTheme(theme: ThemeOption) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(theme = theme)
        notifyWallpaperChanged()
    }

    fun setDotSize(size: DotSize) {
        prefs.edit().putString(KEY_DOT_SIZE, size.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(dotSize = size)
        notifyWallpaperChanged()
    }

    fun setDotShape(shape: DotShape) {
        prefs.edit().putString(KEY_DOT_SHAPE, shape.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(dotShape = shape)
        notifyWallpaperChanged()
    }

    fun setGridDensity(density: GridDensity) {
        prefs.edit().putString(KEY_GRID_DENSITY, density.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(gridDensity = density)
        notifyWallpaperChanged()
    }

    fun setHighlightToday(highlight: Boolean) {
        prefs.edit().putBoolean(KEY_HIGHLIGHT_TODAY, highlight).apply()
        _settingsFlow.value = _settingsFlow.value.copy(highlightToday = highlight)
        notifyWallpaperChanged()
    }

    fun setFilledDotAlpha(alpha: Float) {
        prefs.edit().putFloat(KEY_FILLED_DOT_ALPHA, alpha).apply()
        _settingsFlow.value = _settingsFlow.value.copy(filledDotAlpha = alpha)
        notifyWallpaperChanged()
    }

    fun setEmptyDotAlpha(alpha: Float) {
        prefs.edit().putFloat(KEY_EMPTY_DOT_ALPHA, alpha).apply()
        _settingsFlow.value = _settingsFlow.value.copy(emptyDotAlpha = alpha)
        notifyWallpaperChanged()
    }

    fun setCustomBackgroundColor(color: Int) {
        prefs.edit().putInt(KEY_CUSTOM_BG_COLOR, color).apply()
        val newCustomColors = _settingsFlow.value.customColors.copy(backgroundColor = color)
        _settingsFlow.value = _settingsFlow.value.copy(customColors = newCustomColors)
        notifyWallpaperChanged()
    }

    fun setCustomFilledDotColor(color: Int) {
        prefs.edit().putInt(KEY_CUSTOM_FILLED_COLOR, color).apply()
        val newCustomColors = _settingsFlow.value.customColors.copy(filledDotColor = color)
        _settingsFlow.value = _settingsFlow.value.copy(customColors = newCustomColors)
        notifyWallpaperChanged()
    }

    fun setCustomEmptyDotColor(color: Int) {
        prefs.edit().putInt(KEY_CUSTOM_EMPTY_COLOR, color).apply()
        val newCustomColors = _settingsFlow.value.customColors.copy(emptyDotColor = color)
        _settingsFlow.value = _settingsFlow.value.copy(customColors = newCustomColors)
        notifyWallpaperChanged()
    }

    fun setCustomTodayDotColor(color: Int) {
        prefs.edit().putInt(KEY_CUSTOM_TODAY_COLOR, color).apply()
        val newCustomColors = _settingsFlow.value.customColors.copy(todayDotColor = color)
        _settingsFlow.value = _settingsFlow.value.copy(customColors = newCustomColors)
        notifyWallpaperChanged()
    }

    private fun notifyWallpaperChanged() {
        wallpaperChangeListeners.forEach { it.invoke() }
    }

    companion object {
        private const val PREFS_NAME = "lifedots_prefs"
        private const val KEY_THEME = "theme"
        private const val KEY_DOT_SIZE = "dot_size"
        private const val KEY_DOT_SHAPE = "dot_shape"
        private const val KEY_GRID_DENSITY = "grid_density"
        private const val KEY_HIGHLIGHT_TODAY = "highlight_today"
        private const val KEY_FILLED_DOT_ALPHA = "filled_dot_alpha"
        private const val KEY_EMPTY_DOT_ALPHA = "empty_dot_alpha"
        private const val KEY_CUSTOM_BG_COLOR = "custom_bg_color"
        private const val KEY_CUSTOM_FILLED_COLOR = "custom_filled_color"
        private const val KEY_CUSTOM_EMPTY_COLOR = "custom_empty_color"
        private const val KEY_CUSTOM_TODAY_COLOR = "custom_today_color"

        private val wallpaperChangeListeners = mutableListOf<() -> Unit>()

        fun addWallpaperChangeListener(listener: () -> Unit) {
            wallpaperChangeListeners.add(listener)
        }

        fun removeWallpaperChangeListener(listener: () -> Unit) {
            wallpaperChangeListeners.remove(listener)
        }

        @Volatile
        private var instance: LifeDotsPreferences? = null

        fun getInstance(context: Context): LifeDotsPreferences {
            return instance ?: synchronized(this) {
                instance ?: LifeDotsPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
