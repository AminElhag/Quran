package com.quran.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String,
    val ayahs: List<Ayah> = emptyList()
)

@Serializable
data class Ayah(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val page: Int
)

@Serializable
data class QuranData(
    val surahs: List<Surah>
)

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
