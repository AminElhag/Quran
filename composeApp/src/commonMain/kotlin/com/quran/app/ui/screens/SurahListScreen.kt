package com.quran.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
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
import com.quran.app.ui.components.SurahListItem
import kotlinx.coroutines.flow.collectLatest

enum class ReadingMode {
    SURAH_SCROLL,  // Horizontal scroll between Surahs
    PAGE_READING   // Traditional book/page reading
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahListScreen(
    repository: QuranRepository,
    readingPositionManager: ReadingPositionManager,
    onSurahClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onPageReadingClick: (Int) -> Unit = {},
    onSurahScrollClick: (Int) -> Unit = {}
) {
    var surahs by remember { mutableStateOf<List<Surah>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var lastReadingPosition by remember { mutableStateOf<ReadingPosition?>(null) }
    var lastReadSurah by remember { mutableStateOf<Surah?>(null) }
    var selectedReadingMode by remember { mutableStateOf(ReadingMode.SURAH_SCROLL) }

    LaunchedEffect(Unit) {
        repository.getAllSurahs().collectLatest { surahList ->
            surahs = surahList
            isLoading = false

            // Get last reading position after loading surahs
            val position = readingPositionManager.getLastReadingPosition()
            lastReadingPosition = position
            if (position != null) {
                lastReadSurah = surahList.find { it.number == position.surahNumber }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "القرآن الكريم",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (lastReadingPosition != null && lastReadSurah != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        when (selectedReadingMode) {
                            ReadingMode.SURAH_SCROLL -> onSurahScrollClick(lastReadingPosition!!.surahNumber)
                            ReadingMode.PAGE_READING -> onSurahClick(lastReadingPosition!!.surahNumber)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text("متابعة القراءة")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    // Header
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "اختر سورة للقراءة",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Reading Mode Selector
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "وضع القراءة",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SegmentedButton(
                                    selected = selectedReadingMode == ReadingMode.SURAH_SCROLL,
                                    onClick = { selectedReadingMode = ReadingMode.SURAH_SCROLL },
                                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = MaterialTheme.colorScheme.primary,
                                        activeContentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text("تصفح السور")
                                }
                                SegmentedButton(
                                    selected = selectedReadingMode == ReadingMode.PAGE_READING,
                                    onClick = { selectedReadingMode = ReadingMode.PAGE_READING },
                                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = MaterialTheme.colorScheme.primary,
                                        activeContentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text("قراءة الصفحات")
                                }
                            }
                        }
                    }
                }

                // Resume Reading Card
                if (lastReadingPosition != null && lastReadSurah != null) {
                    item {
                        Card(
                            onClick = {
                                when (selectedReadingMode) {
                                    ReadingMode.SURAH_SCROLL -> onSurahScrollClick(lastReadingPosition!!.surahNumber)
                                    ReadingMode.PAGE_READING -> onSurahClick(lastReadingPosition!!.surahNumber)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "متابعة القراءة",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "سورة ${lastReadSurah!!.name} - الآية ${lastReadingPosition!!.ayahIndex + 1}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                items(surahs) { surah ->
                    SurahListItem(
                        surah = surah,
                        onClick = {
                            when (selectedReadingMode) {
                                ReadingMode.SURAH_SCROLL -> onSurahScrollClick(surah.number)
                                ReadingMode.PAGE_READING -> onSurahClick(surah.number)
                            }
                        }
                    )
                }
            }
        }
    }
}
