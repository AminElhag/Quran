package com.quran.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import quranapp.composeapp.generated.resources.Res

class QuranRepository {
    private var cachedQuranData: QuranData? = null

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadQuranData(): QuranData {
        if (cachedQuranData != null) {
            return cachedQuranData!!
        }

        val bytes = Res.readBytes("files/quran.json")
        val jsonString = bytes.decodeToString()
        cachedQuranData = json.decodeFromString<QuranData>(jsonString)
        return cachedQuranData!!
    }

    fun getAllSurahs(): Flow<List<Surah>> = flow {
        val data = loadQuranData()
        emit(data.surahs)
    }

    fun getSurah(number: Int): Flow<Surah?> = flow {
        val data = loadQuranData()
        emit(data.surahs.find { it.number == number })
    }

    fun searchAyahs(query: String): Flow<List<Pair<Surah, Ayah>>> = flow {
        val data = loadQuranData()
        val results = mutableListOf<Pair<Surah, Ayah>>()
        data.surahs.forEach { surah ->
            surah.ayahs.forEach { ayah ->
                if (ayah.text.contains(query)) {
                    results.add(Pair(surah, ayah))
                }
            }
        }
        emit(results)
    }

    fun getPageContent(pageNumber: Int): Flow<PageContent> = flow {
        val data = loadQuranData()
        val ayahsWithSurah = mutableListOf<AyahWithSurah>()

        data.surahs.forEach { surah ->
            surah.ayahs.forEach { ayah ->
                if (ayah.page == pageNumber) {
                    ayahsWithSurah.add(AyahWithSurah(ayah, surah))
                }
            }
        }

        emit(PageContent(pageNumber, ayahsWithSurah))
    }

    fun getAllPages(): Flow<Map<Int, PageContent>> = flow {
        val data = loadQuranData()
        val pagesMap = mutableMapOf<Int, MutableList<AyahWithSurah>>()

        data.surahs.forEach { surah ->
            surah.ayahs.forEach { ayah ->
                if (!pagesMap.containsKey(ayah.page)) {
                    pagesMap[ayah.page] = mutableListOf()
                }
                pagesMap[ayah.page]!!.add(AyahWithSurah(ayah, surah))
            }
        }

        val result = pagesMap.mapValues { (pageNumber, ayahs) ->
            PageContent(pageNumber, ayahs)
        }

        emit(result)
    }

    fun getTotalPages(): Flow<Int> = flow {
        val data = loadQuranData()
        val maxPage = data.surahs.flatMap { it.ayahs }.maxOfOrNull { it.page } ?: 604
        emit(maxPage)
    }

    fun getPageForSurah(surahNumber: Int): Flow<Int> = flow {
        val data = loadQuranData()
        val surah = data.surahs.find { it.number == surahNumber }
        val firstPage = surah?.ayahs?.firstOrNull()?.page ?: 1
        emit(firstPage)
    }
}
