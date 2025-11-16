package com.quran.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class QuranRepository {
    private val quranData: QuranData by lazy { createQuranData() }

    fun getAllSurahs(): Flow<List<Surah>> = flow {
        emit(quranData.surahs)
    }

    fun getSurah(number: Int): Flow<Surah?> = flow {
        emit(quranData.surahs.find { it.number == number })
    }

    fun searchAyahs(query: String): Flow<List<Pair<Surah, Ayah>>> = flow {
        val results = mutableListOf<Pair<Surah, Ayah>>()
        quranData.surahs.forEach { surah ->
            surah.ayahs.forEach { ayah ->
                if (ayah.text.contains(query)) {
                    results.add(Pair(surah, ayah))
                }
            }
        }
        emit(results)
    }

    private fun createQuranData(): QuranData {
        return QuranData(
            surahs = listOf(
                Surah(
                    number = 1,
                    name = "الفاتحة",
                    englishName = "Al-Fatihah",
                    englishNameTranslation = "The Opening",
                    numberOfAyahs = 7,
                    revelationType = "Meccan",
                    ayahs = listOf(
                        Ayah(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", 1, 1, 1),
                        Ayah(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", 2, 1, 1),
                        Ayah(3, "الرَّحْمَٰنِ الرَّحِيمِ", 3, 1, 1),
                        Ayah(4, "مَالِكِ يَوْمِ الدِّينِ", 4, 1, 1),
                        Ayah(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", 5, 1, 1),
                        Ayah(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", 6, 1, 1),
                        Ayah(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", 7, 1, 2)
                    )
                ),
                Surah(
                    number = 2,
                    name = "البقرة",
                    englishName = "Al-Baqarah",
                    englishNameTranslation = "The Cow",
                    numberOfAyahs = 286,
                    revelationType = "Medinan",
                    ayahs = listOf(
                        Ayah(8, "الم", 1, 1, 2),
                        Ayah(9, "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ", 2, 1, 2),
                        Ayah(10, "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ", 3, 1, 2),
                        Ayah(11, "وَالَّذِينَ يُؤْمِنُونَ بِمَا أُنزِلَ إِلَيْكَ وَمَا أُنزِلَ مِن قَبْلِكَ وَبِالْآخِرَةِ هُمْ يُوقِنُونَ", 4, 1, 2),
                        Ayah(12, "أُولَٰئِكَ عَلَىٰ هُدًى مِّن رَّبِّهِمْ ۖ وَأُولَٰئِكَ هُمُ الْمُفْلِحُونَ", 5, 1, 3)
                    )
                ),
                Surah(
                    number = 112,
                    name = "الإخلاص",
                    englishName = "Al-Ikhlas",
                    englishNameTranslation = "Sincerity",
                    numberOfAyahs = 4,
                    revelationType = "Meccan",
                    ayahs = listOf(
                        Ayah(6223, "قُلْ هُوَ اللَّهُ أَحَدٌ", 1, 30, 604),
                        Ayah(6224, "اللَّهُ الصَّمَدُ", 2, 30, 604),
                        Ayah(6225, "لَمْ يَلِدْ وَلَمْ يُولَدْ", 3, 30, 604),
                        Ayah(6226, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", 4, 30, 604)
                    )
                ),
                Surah(
                    number = 113,
                    name = "الفلق",
                    englishName = "Al-Falaq",
                    englishNameTranslation = "The Daybreak",
                    numberOfAyahs = 5,
                    revelationType = "Meccan",
                    ayahs = listOf(
                        Ayah(6227, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", 1, 30, 604),
                        Ayah(6228, "مِن شَرِّ مَا خَلَقَ", 2, 30, 604),
                        Ayah(6229, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", 3, 30, 604),
                        Ayah(6230, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", 4, 30, 604),
                        Ayah(6231, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", 5, 30, 604)
                    )
                ),
                Surah(
                    number = 114,
                    name = "الناس",
                    englishName = "An-Nas",
                    englishNameTranslation = "Mankind",
                    numberOfAyahs = 6,
                    revelationType = "Meccan",
                    ayahs = listOf(
                        Ayah(6232, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", 1, 30, 604),
                        Ayah(6233, "مَلِكِ النَّاسِ", 2, 30, 604),
                        Ayah(6234, "إِلَٰهِ النَّاسِ", 3, 30, 604),
                        Ayah(6235, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", 4, 30, 604),
                        Ayah(6236, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", 5, 30, 604),
                        Ayah(6237, "مِنَ الْجِنَّةِ وَالنَّاسِ", 6, 30, 604)
                    )
                ),
                Surah(
                    number = 103,
                    name = "العصر",
                    englishName = "Al-Asr",
                    englishNameTranslation = "The Declining Day",
                    numberOfAyahs = 3,
                    revelationType = "Meccan",
                    ayahs = listOf(
                        Ayah(6188, "وَالْعَصْرِ", 1, 30, 601),
                        Ayah(6189, "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ", 2, 30, 601),
                        Ayah(6190, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", 3, 30, 601)
                    )
                ),
                Surah(
                    number = 108,
                    name = "الكوثر",
                    englishName = "Al-Kawthar",
                    englishNameTranslation = "Abundance",
                    numberOfAyahs = 3,
                    revelationType = "Meccan",
                    ayahs = listOf(
                        Ayah(6205, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", 1, 30, 602),
                        Ayah(6206, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", 2, 30, 602),
                        Ayah(6207, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", 3, 30, 602)
                    )
                ),
                Surah(
                    number = 110,
                    name = "النصر",
                    englishName = "An-Nasr",
                    englishNameTranslation = "Divine Support",
                    numberOfAyahs = 3,
                    revelationType = "Medinan",
                    ayahs = listOf(
                        Ayah(6214, "إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ", 1, 30, 603),
                        Ayah(6215, "وَرَأَيْتَ النَّاسَ يَدْخُلُونَ فِي دِينِ اللَّهِ أَفْوَاجًا", 2, 30, 603),
                        Ayah(6216, "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ ۚ إِنَّهُ كَانَ تَوَّابًا", 3, 30, 603)
                    )
                ),
                Surah(
                    number = 36,
                    name = "يس",
                    englishName = "Ya-Sin",
                    englishNameTranslation = "Ya Sin",
                    numberOfAyahs = 83,
                    revelationType = "Meccan",
                    ayahs = listOf(
                        Ayah(2678, "يس", 1, 22, 440),
                        Ayah(2679, "وَالْقُرْآنِ الْحَكِيمِ", 2, 22, 440),
                        Ayah(2680, "إِنَّكَ لَمِنَ الْمُرْسَلِينَ", 3, 22, 440),
                        Ayah(2681, "عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ", 4, 22, 440),
                        Ayah(2682, "تَنزِيلَ الْعَزِيزِ الرَّحِيمِ", 5, 22, 440)
                    )
                ),
                Surah(
                    number = 55,
                    name = "الرحمن",
                    englishName = "Ar-Rahman",
                    englishNameTranslation = "The Beneficent",
                    numberOfAyahs = 78,
                    revelationType = "Medinan",
                    ayahs = listOf(
                        Ayah(4740, "الرَّحْمَٰنُ", 1, 27, 531),
                        Ayah(4741, "عَلَّمَ الْقُرْآنَ", 2, 27, 531),
                        Ayah(4742, "خَلَقَ الْإِنسَانَ", 3, 27, 531),
                        Ayah(4743, "عَلَّمَهُ الْبَيَانَ", 4, 27, 531),
                        Ayah(4744, "الشَّمْسُ وَالْقَمَرُ بِحُسْبَانٍ", 5, 27, 531)
                    )
                )
            ).sortedBy { it.number }
        )
    }
}
