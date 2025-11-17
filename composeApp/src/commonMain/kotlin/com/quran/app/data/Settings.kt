package com.quran.app.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

expect class Settings() {
    fun putString(key: String, value: String)
    fun getString(key: String, defaultValue: String): String
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
