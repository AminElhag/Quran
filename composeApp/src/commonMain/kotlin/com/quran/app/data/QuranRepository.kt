package com.quran.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import quranapp.composeapp.generated.resources.Res

class QuranRepository {
    private var cachedSurahs: List<Surah>? = null
    private var cachedPageMapping: Map<Int, List<PageMapping>>? = null

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

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadPageMapping(): Map<Int, List<PageMapping>> {
        if (cachedPageMapping != null) {
            return cachedPageMapping!!
        }

        val bytes = Res.readBytes("files/page_mapping.json")
        val jsonString = bytes.decodeToString()
        cachedPageMapping = json.decodeFromString<Map<Int, List<PageMapping>>>(jsonString)
        return cachedPageMapping!!
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
        val surahs = loadQuranData()
        val pageMapping = loadPageMapping()

        val pageAyahs = pageMapping[pageNumber] ?: emptyList()
        val ayahsWithSurahs = mutableListOf<AyahWithSurah>()

        for (mapping in pageAyahs) {
            val surah = surahs.find { it.number == mapping.surah }
            if (surah != null) {
                val ayah = surah.ayahs.find { it.numberInSurah == mapping.ayah }
                if (ayah != null) {
                    ayahsWithSurahs.add(AyahWithSurah(ayah, surah))
                }
            }
        }

        emit(PageContent(pageNumber, ayahsWithSurahs))
    }

    fun getAllPages(): Flow<Map<Int, PageContent>> = flow {
        val surahs = loadQuranData()
        val pageMapping = loadPageMapping()

        val pagesMap = mutableMapOf<Int, PageContent>()

        for ((pageNumber, mappings) in pageMapping) {
            val ayahsWithSurahs = mutableListOf<AyahWithSurah>()

            for (mapping in mappings) {
                val surah = surahs.find { it.number == mapping.surah }
                if (surah != null) {
                    val ayah = surah.ayahs.find { it.numberInSurah == mapping.ayah }
                    if (ayah != null) {
                        ayahsWithSurahs.add(AyahWithSurah(ayah, surah))
                    }
                }
            }

            pagesMap[pageNumber] = PageContent(pageNumber, ayahsWithSurahs)
        }

        emit(pagesMap)
    }

    fun getTotalPages(): Flow<Int> = flow {
        // Default Quran page count
        emit(604)
    }

    fun getPageForSurah(surahNumber: Int): Flow<Int> = flow {
        val pageMapping = loadPageMapping()

        // Find the first page that contains the first ayah of this surah
        for ((pageNumber, mappings) in pageMapping) {
            val firstAyahOfSurah = mappings.find { it.surah == surahNumber && it.ayah == 1 }
            if (firstAyahOfSurah != null) {
                emit(pageNumber)
                return@flow
            }
        }

        // Default to page 1 if not found
        emit(1)
    }
}
