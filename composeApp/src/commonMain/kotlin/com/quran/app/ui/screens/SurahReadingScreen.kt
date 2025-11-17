package com.quran.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
    var surah by remember { mutableStateOf<Surah?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    var hasRestoredPosition by remember { mutableStateOf(false) }

    LaunchedEffect(surahNumber) {
        repository.getSurah(surahNumber).collectLatest { s ->
            surah = s
            isLoading = false
        }
    }

    // Restore scroll position if returning to the same surah
    LaunchedEffect(surah, hasRestoredPosition) {
        if (surah != null && !hasRestoredPosition) {
            val lastPosition = readingPositionManager.getLastReadingPosition()
            if (lastPosition != null && lastPosition.surahNumber == surahNumber) {
                // Calculate the actual index considering header and bismillah
                val headerOffset = if (surahNumber != 9 && surahNumber != 1) 2 else 1
                val targetIndex = lastPosition.ayahIndex + headerOffset
                if (targetIndex > 0 && targetIndex < surah!!.ayahs.size + headerOffset + 1) {
                    listState.scrollToItem(targetIndex)
                }
            }
            hasRestoredPosition = true
        }
    }

    // Save reading position when scrolling
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (surah != null && hasRestoredPosition) {
            // Debounce saving to avoid too frequent saves
            delay(500)

            // Calculate the ayah index from the list position
            val headerOffset = if (surahNumber != 9 && surahNumber != 1) 2 else 1
            val ayahIndex = (listState.firstVisibleItemIndex - headerOffset).coerceAtLeast(0)

            if (ayahIndex < surah!!.ayahs.size) {
                val position = ReadingPosition(
                    surahNumber = surahNumber,
                    ayahIndex = ayahIndex,
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
                        text = surah?.name ?: "جاري التحميل...",
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
        } else if (surah == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لم يتم العثور على السورة",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                                text = "سورة ${surah!!.name}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = surah!!.englishName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${surah!!.numberOfAyahs} آيات - ${if (surah!!.revelationType == "Meccan") "مكية" else "مدنية"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Bismillah (except for Surah At-Tawbah which is number 9)
                if (surahNumber != 9 && surahNumber != 1) {
                    item {
                        Bismillah()
                    }
                }

                // Ayahs
                items(surah!!.ayahs) { ayah ->
                    AyahItem(ayah = ayah)
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
