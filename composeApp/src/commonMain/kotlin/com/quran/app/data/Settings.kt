package com.quran.app.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

expect class Settings() {
    fun putString(key: String, value: String)
    fun getString(key: String, defaultValue: String): String
    fun putInt(key: String, value: Int)
    fun getInt(key: String, defaultValue: Int): Int
    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun remove(key: String)
    fun clear()
}

class ReadingPositionManager(private val settings: Settings) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val KEY_READING_POSITION = "reading_position"
        private const val KEY_PAGE_READING_POSITION = "page_reading_position"
    }

    fun saveReadingPosition(position: ReadingPosition) {
        val jsonString = json.encodeToString(position)
        settings.putString(KEY_READING_POSITION, jsonString)
    }

    fun getLastReadingPosition(): ReadingPosition? {
        val jsonString = settings.getString(KEY_READING_POSITION, "")
        return if (jsonString.isNotEmpty()) {
            try {
                json.decodeFromString<ReadingPosition>(jsonString)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun clearReadingPosition() {
        settings.remove(KEY_READING_POSITION)
    }

    fun savePageReadingPosition(position: PageReadingPosition) {
        val jsonString = json.encodeToString(position)
        settings.putString(KEY_PAGE_READING_POSITION, jsonString)
    }

    fun getLastPageReadingPosition(): PageReadingPosition? {
        val jsonString = settings.getString(KEY_PAGE_READING_POSITION, "")
        return if (jsonString.isNotEmpty()) {
            try {
                json.decodeFromString<PageReadingPosition>(jsonString)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun clearPageReadingPosition() {
        settings.remove(KEY_PAGE_READING_POSITION)
    }
}

class TextSizeManager(private val settings: Settings) {
    companion object {
        private const val KEY_TEXT_SIZE_LEVEL = "text_size_level"
        private const val KEY_FULL_PAGE_MODE = "full_page_mode"
        const val MIN_SIZE_LEVEL = 1
        const val MAX_SIZE_LEVEL = 5
        const val DEFAULT_SIZE_LEVEL = 3
    }

    fun saveTextSizeLevel(level: Int) {
        val validLevel = level.coerceIn(MIN_SIZE_LEVEL, MAX_SIZE_LEVEL)
        settings.putInt(KEY_TEXT_SIZE_LEVEL, validLevel)
    }

    fun getTextSizeLevel(): Int {
        return settings.getInt(KEY_TEXT_SIZE_LEVEL, DEFAULT_SIZE_LEVEL)
    }

    fun saveFullPageMode(enabled: Boolean) {
        settings.putBoolean(KEY_FULL_PAGE_MODE, enabled)
    }

    fun isFullPageModeEnabled(): Boolean {
        return settings.getBoolean(KEY_FULL_PAGE_MODE, false)
    }

    // Helper function to get font size multiplier based on level
    fun getTextSizeMultiplier(level: Int = getTextSizeLevel()): Float {
        return when (level) {
            1 -> 0.75f  // Small
            2 -> 0.875f // Medium-Small
            3 -> 1.0f   // Normal (default)
            4 -> 1.125f // Medium-Large
            5 -> 1.25f  // Large
            else -> 1.0f
        }
    }
}

class TajweedSettingsManager(private val settings: Settings) {
    companion object {
        private const val KEY_TAJWEED_ENABLED = "tajweed_enabled"
        private const val KEY_AUTO_DETECT_TAJWEED = "auto_detect_tajweed"
        private const val KEY_SHOW_TAJWEED_LEGEND = "show_tajweed_legend"
    }

    fun saveTajweedEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_TAJWEED_ENABLED, enabled)
    }

    fun isTajweedEnabled(): Boolean {
        return settings.getBoolean(KEY_TAJWEED_ENABLED, true) // Enabled by default
    }

    fun saveAutoDetectTajweed(enabled: Boolean) {
        settings.putBoolean(KEY_AUTO_DETECT_TAJWEED, enabled)
    }

    fun isAutoDetectTajweedEnabled(): Boolean {
        return settings.getBoolean(KEY_AUTO_DETECT_TAJWEED, true) // Enabled by default
    }

    fun saveShowTajweedLegend(show: Boolean) {
        settings.putBoolean(KEY_SHOW_TAJWEED_LEGEND, show)
    }

    fun shouldShowTajweedLegend(): Boolean {
        return settings.getBoolean(KEY_SHOW_TAJWEED_LEGEND, false)
    }
}
