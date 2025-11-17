package com.quran.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quran.app.data.QuranRepository
import com.quran.app.data.ReadingPosition
import com.quran.app.data.ReadingPositionManager
import com.quran.app.data.Surah
import com.quran.app.data.currentTimeMillis
import com.quran.app.ui.components.AyahItem
import com.quran.app.ui.components.Bismillah
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReadingScreen(
    surahNumber: Int,
    repository: QuranRepository,
    readingPositionManager: ReadingPositionManager,
    onBackClick: () -> Unit
) {
    var allSurahs by remember { mutableStateOf<List<Surah>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var startSurahIndex by remember { mutableStateOf(surahNumber - 1) }

    // Load all surahs for horizontal scrolling
    LaunchedEffect(Unit) {
        repository.getAllSurahs().collectLatest { surahs ->
            allSurahs = surahs
            isLoading = false
        }
    }

    // Setup pager state for horizontal scrolling
    val pagerState = rememberPagerState(
        initialPage = startSurahIndex,
        pageCount = { allSurahs.size.coerceAtLeast(1) }
    )

    // Get current surah based on pager position
    val currentSurah = remember(pagerState.currentPage, allSurahs) {
        if (allSurahs.isNotEmpty() && pagerState.currentPage < allSurahs.size) {
            allSurahs[pagerState.currentPage]
        } else null
    }

    // Save reading position when surah changes
    LaunchedEffect(pagerState.currentPage) {
        if (!isLoading && allSurahs.isNotEmpty()) {
            delay(300) // Debounce
            val currentSurahNumber = pagerState.currentPage + 1
            if (currentSurahNumber <= allSurahs.size) {
                val position = ReadingPosition(
                    surahNumber = currentSurahNumber,
                    ayahIndex = 0,
                    timestamp = currentTimeMillis()
                )
                readingPositionManager.saveReadingPosition(position)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentSurah?.name ?: "جاري التحميل...",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (allSurahs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لم يتم العثور على السور",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // Horizontal pager for swiping between surahs
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                reverseLayout = true, // RTL support for Arabic
                key = { it }
            ) { pageIndex ->
                val surah = allSurahs[pageIndex]
                SurahContent(
                    surah = surah,
                    readingPositionManager = readingPositionManager,
                    isCurrentPage = pageIndex == pagerState.currentPage
                )
            }
        }
    }
}

@Composable
private fun SurahContent(
    surah: Surah,
    readingPositionManager: ReadingPositionManager,
    isCurrentPage: Boolean
) {
    val listState = rememberLazyListState()
    var hasRestoredPosition by remember { mutableStateOf(false) }

    // Restore scroll position if returning to the same surah
    LaunchedEffect(surah, hasRestoredPosition, isCurrentPage) {
        if (!hasRestoredPosition && isCurrentPage) {
            val lastPosition = readingPositionManager.getLastReadingPosition()
            if (lastPosition != null && lastPosition.surahNumber == surah.number) {
                // Calculate the actual index considering header and bismillah
                val headerOffset = if (surah.number != 9 && surah.number != 1) 2 else 1
                val targetIndex = lastPosition.ayahIndex + headerOffset
                if (targetIndex > 0 && targetIndex < surah.ayahs.size + headerOffset + 1) {
                    listState.scrollToItem(targetIndex)
                }
            }
            hasRestoredPosition = true
        }
    }

    // Save reading position when scrolling within the surah
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (hasRestoredPosition && isCurrentPage) {
            // Debounce saving to avoid too frequent saves
            delay(500)

            // Calculate the ayah index from the list position
            val headerOffset = if (surah.number != 9 && surah.number != 1) 2 else 1
            val ayahIndex = (listState.firstVisibleItemIndex - headerOffset).coerceAtLeast(0)

            if (ayahIndex < surah.ayahs.size) {
                val position = ReadingPosition(
                    surahNumber = surah.number,
                    ayahIndex = ayahIndex,
                    timestamp = currentTimeMillis()
                )
                readingPositionManager.saveReadingPosition(position)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        // Surah Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "سورة ${surah.name}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = surah.englishName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${surah.numberOfAyahs} آيات - ${if (surah.revelationType == "Meccan") "مكية" else "مدنية"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bismillah (except for Surah At-Tawbah which is number 9)
        if (surah.number != 9 && surah.number != 1) {
            item {
                Bismillah()
            }
        }

        // Ayahs
        items(surah.ayahs) { ayah ->
            AyahItem(ayah = ayah)
        }

        // Bottom spacing and next surah indicator
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (surah.number < 114) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "← اسحب للسورة التالية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "ختام القرآن الكريم",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
