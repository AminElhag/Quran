package com.quran.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quran.app.data.*
import com.quran.app.ui.components.Bismillah
import com.quran.app.ui.theme.QuranTextStyles
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageReadingScreen(
    initialPage: Int = 1,
    repository: QuranRepository,
    readingPositionManager: ReadingPositionManager,
    onBackClick: () -> Unit,
    onSurahListClick: () -> Unit
) {
    var pagesMap by remember { mutableStateOf<Map<Int, PageContent>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var totalPages by remember { mutableStateOf(604) }
    val scope = rememberCoroutineScope()

    // Determine starting page (from saved position or parameter)
    var startPage by remember { mutableStateOf(initialPage) }

    LaunchedEffect(Unit) {
        // Check for saved page reading position
        val savedPosition = readingPositionManager.getLastPageReadingPosition()
        if (savedPosition != null && initialPage == 1) {
            startPage = savedPosition.pageNumber
        }
    }

    // Load all pages data
    LaunchedEffect(Unit) {
        repository.getAllPages().collectLatest { pages ->
            pagesMap = pages
            totalPages = pages.keys.maxOrNull() ?: 604
            isLoading = false
        }
    }

    val pagerState = rememberPagerState(
        initialPage = startPage - 1, // 0-indexed
        pageCount = { totalPages }
    )

    // Save reading position when page changes
    LaunchedEffect(pagerState.currentPage) {
        if (!isLoading) {
            val currentPageNumber = pagerState.currentPage + 1
            val position = PageReadingPosition(
                pageNumber = currentPageNumber,
                timestamp = currentTimeMillis()
            )
            readingPositionManager.savePageReadingPosition(position)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "صفحة ${pagerState.currentPage + 1}",
                        style = MaterialTheme.typography.titleLarge,
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
                actions = {
                    IconButton(onClick = onSurahListClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "قائمة السور"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            PageNavigationBar(
                currentPage = pagerState.currentPage + 1,
                totalPages = totalPages,
                onPageSelected = { page ->
                    scope.launch {
                        pagerState.animateScrollToPage(page - 1)
                    }
                },
                onPreviousPage = {
                    scope.launch {
                        if (pagerState.currentPage > 0) {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                onNextPage = {
                    scope.launch {
                        if (pagerState.currentPage < totalPages - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "جاري تحميل الصفحات...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                reverseLayout = true, // RTL - swipe left to go forward
                key = { it }
            ) { pageIndex ->
                val pageNumber = pageIndex + 1
                val pageContent = pagesMap[pageNumber]

                if (pageContent != null) {
                    QuranPage(
                        pageContent = pageContent,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "صفحة فارغة",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuranPage(
    pageContent: PageContent,
    modifier: Modifier = Modifier
) {
    var currentSurahNumber by remember { mutableStateOf(-1) }

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(pageContent.ayahs) { ayahWithSurah ->
            val showSurahHeader = ayahWithSurah.ayah.numberInSurah == 1 &&
                                   ayahWithSurah.surah.number != currentSurahNumber

            if (showSurahHeader) {
                currentSurahNumber = ayahWithSurah.surah.number

                // Surah header
                SurahHeaderInPage(surah = ayahWithSurah.surah)

                // Bismillah (except for At-Tawbah and if not first ayah of Al-Fatiha which includes it)
                if (ayahWithSurah.surah.number != 9 && ayahWithSurah.surah.number != 1) {
                    Bismillah()
                }
            }

            // Display ayah with inline number
            PageAyahItem(
                ayah = ayahWithSurah.ayah,
                surahName = ayahWithSurah.surah.name
            )
        }

        // Page footer
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "صفحة ${pageContent.pageNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SurahHeaderInPage(surah: Surah) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "سورة ${surah.name}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${surah.englishName} - ${surah.numberOfAyahs} آيات",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PageAyahItem(
    ayah: Ayah,
    surahName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top
    ) {
        // Ayah text with number marker
        Text(
            text = buildString {
                append(ayah.text)
                append(" ")
                append("﴿${ayah.numberInSurah}﴾")
            },
            style = QuranTextStyles.ayahText,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PageNavigationBar(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    var showPagePicker by remember { mutableStateOf(false) }
    var pageInputText by remember { mutableStateOf(currentPage.toString()) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Next page (RTL - right side is next)
            IconButton(
                onClick = onNextPage,
                enabled = currentPage < totalPages
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "الصفحة التالية"
                )
            }

            // Page indicator - clickable to jump to page
            TextButton(
                onClick = {
                    pageInputText = currentPage.toString()
                    showPagePicker = true
                }
            ) {
                Text(
                    text = "$currentPage / $totalPages",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Previous page (RTL - left side is previous)
            IconButton(
                onClick = onPreviousPage,
                enabled = currentPage > 1
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "الصفحة السابقة"
                )
            }
        }
    }

    // Page picker dialog
    if (showPagePicker) {
        AlertDialog(
            onDismissRequest = { showPagePicker = false },
            title = {
                Text(
                    text = "انتقل إلى صفحة",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = pageInputText,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                pageInputText = newValue
                            }
                        },
                        label = { Text("رقم الصفحة (1-$totalPages)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val page = pageInputText.toIntOrNull()
                        if (page != null && page in 1..totalPages) {
                            onPageSelected(page)
                            showPagePicker = false
                        }
                    }
                ) {
                    Text("انتقل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPagePicker = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
