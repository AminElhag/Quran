package com.quran.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import quranapp.composeapp.generated.resources.Res

class QuranRepository {
    private var cachedSurahs: List<Surah>? = null

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadQuranData(): List<Surah> {
        if (cachedSurahs != null) {
            return cachedSurahs!!
        }

        val bytes = Res.readBytes("files/quran.json")
        val jsonString = bytes.decodeToString()
        cachedSurahs = json.decodeFromString<List<Surah>>(jsonString)
        return cachedSurahs!!
    }

    fun getAllSurahs(): Flow<List<Surah>> = flow {
        val data = loadQuranData()
        emit(data)
    }

    fun getSurah(number: Int): Flow<Surah?> = flow {
        val data = loadQuranData()
        emit(data.find { it.number == number })
    }

    fun searchAyahs(query: String): Flow<List<Pair<Surah, Ayah>>> = flow {
        val data = loadQuranData()
        val results = mutableListOf<Pair<Surah, Ayah>>()
        data.forEach { surah ->
            surah.ayahs.forEach { ayah ->
                if (ayah.text.contains(query)) {
                    results.add(Pair(surah, ayah))
                }
            }
        }
        emit(results)
    }

    fun getPageContent(pageNumber: Int): Flow<PageContent> = flow {
        // Note: Page information is not available in new JSON format
        // This returns empty content - page reading feature will not work
        emit(PageContent(pageNumber, emptyList()))
    }

    fun getAllPages(): Flow<Map<Int, PageContent>> = flow {
        // Note: Page information is not available in new JSON format
        // This returns empty map - page reading feature will not work
        emit(emptyMap())
    }

    fun getTotalPages(): Flow<Int> = flow {
        // Default Quran page count
        emit(604)
    }

    fun getPageForSurah(surahNumber: Int): Flow<Int> = flow {
        // Note: Page information is not available in new JSON format
        // This returns 1 as default
        emit(1)
    }
}
