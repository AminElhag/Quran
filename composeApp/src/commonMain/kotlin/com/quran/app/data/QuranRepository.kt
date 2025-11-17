package com.quran.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import quran.composeapp.generated.resources.Res

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
}
