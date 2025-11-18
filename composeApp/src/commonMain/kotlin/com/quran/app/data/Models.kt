package com.quran.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Surah(
    val id: Int,
    val name: String,
    val transliteration: String,
    val type: String,
    val total_verses: Int,
    val verses: List<Ayah> = emptyList()
) {
    // Computed properties to maintain backward compatibility
    val number: Int get() = id
    val englishName: String get() = transliteration
    val englishNameTranslation: String get() = transliteration
    val numberOfAyahs: Int get() = total_verses
    val revelationType: String get() = when(type.lowercase()) {
        "meccan" -> "Meccan"
        "medinan" -> "Medinan"
        else -> type
    }
    val ayahs: List<Ayah> get() = verses
}

@Serializable
data class Ayah(
    val id: Int,
    val text: String
) {
    // Computed properties to maintain backward compatibility
    val number: Int get() = id
    val numberInSurah: Int get() = id
    // Note: juz and page are not available in new JSON format
    // These return default values - page/juz features will not work correctly
    val juz: Int get() = 0
    val page: Int get() = 0
}

enum class RevelationType(val arabic: String) {
    MECCAN("مكية"),
    MEDINAN("مدنية")
}

@Serializable
data class ReadingPosition(
    val surahNumber: Int,
    val ayahIndex: Int,
    val timestamp: Long
)

@Serializable
data class PageReadingPosition(
    val pageNumber: Int,
    val timestamp: Long
)

data class PageContent(
    val pageNumber: Int,
    val ayahs: List<AyahWithSurah>
)

data class AyahWithSurah(
    val ayah: Ayah,
    val surah: Surah
)
