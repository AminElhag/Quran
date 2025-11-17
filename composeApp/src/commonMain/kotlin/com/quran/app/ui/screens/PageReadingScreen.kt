package com.quran.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quran.app.data.*
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

    var startPage by remember { mutableStateOf(initialPage) }

    LaunchedEffect(Unit) {
        val savedPosition = readingPositionManager.getLastPageReadingPosition()
        if (savedPosition != null && initialPage == 1) {
            startPage = savedPosition.pageNumber
        }
    }

    LaunchedEffect(Unit) {
        repository.getAllPages().collectLatest { pages ->
            pagesMap = pages
            totalPages = pages.keys.maxOrNull() ?: 604
            isLoading = false
        }
    }

    val pagerState = rememberPagerState(
        initialPage = startPage - 1,
        pageCount = { totalPages }
    )

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
                reverseLayout = true,
                key = { it }
            ) { pageIndex ->
                val pageNumber = pageIndex + 1
                val pageContent = pagesMap[pageNumber]

                if (pageContent != null) {
                    TraditionalQuranPage(
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
private fun TraditionalQuranPage(
    pageContent: PageContent,
    modifier: Modifier = Modifier
) {
    val frameColor = Color(0xFF8B7355)
    val innerFrameColor = Color(0xFFB8A082)
    val pageBackgroundColor = Color(0xFFFFF8E7)

    Box(
        modifier = modifier
            .padding(12.dp)
            .background(pageBackgroundColor, RoundedCornerShape(4.dp))
            .border(3.dp, frameColor, RoundedCornerShape(4.dp))
    ) {
        // Inner decorative border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .border(1.dp, innerFrameColor, RoundedCornerShape(2.dp))
        ) {
            // Corner decorations
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val cornerSize = 20.dp.toPx()
                        val strokeWidth = 2.dp.toPx()

                        // Top-left corner decoration
                        drawLine(
                            color = frameColor,
                            start = Offset(0f, cornerSize),
                            end = Offset(0f, 0f),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = frameColor,
                            start = Offset(0f, 0f),
                            end = Offset(cornerSize, 0f),
                            strokeWidth = strokeWidth
                        )

                        // Top-right corner decoration
                        drawLine(
                            color = frameColor,
                            start = Offset(size.width - cornerSize, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = frameColor,
                            start = Offset(size.width, 0f),
                            end = Offset(size.width, cornerSize),
                            strokeWidth = strokeWidth
                        )

                        // Bottom-left corner decoration
                        drawLine(
                            color = frameColor,
                            start = Offset(0f, size.height - cornerSize),
                            end = Offset(0f, size.height),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = frameColor,
                            start = Offset(0f, size.height),
                            end = Offset(cornerSize, size.height),
                            strokeWidth = strokeWidth
                        )

                        // Bottom-right corner decoration
                        drawLine(
                            color = frameColor,
                            start = Offset(size.width - cornerSize, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = frameColor,
                            start = Offset(size.width, size.height - cornerSize),
                            end = Offset(size.width, size.height),
                            strokeWidth = strokeWidth
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Build page content with traditional styling
                    var lastSurahNumber = -1

                    pageContent.ayahs.forEach { ayahWithSurah ->
                        val isNewSurah = ayahWithSurah.ayah.numberInSurah == 1 &&
                                         ayahWithSurah.surah.number != lastSurahNumber

                        if (isNewSurah) {
                            lastSurahNumber = ayahWithSurah.surah.number

                            // Surah header with traditional styling
                            OrnamentedSurahHeader(surah = ayahWithSurah.surah)

                            Spacer(modifier = Modifier.height(12.dp))

                            // Bismillah (except for At-Tawbah and Al-Fatiha)
                            if (ayahWithSurah.surah.number != 9 && ayahWithSurah.surah.number != 1) {
                                TraditionalBismillah()
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    // Flowing Quranic text
                    FlowingQuranText(
                        ayahs = pageContent.ayahs,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Page number at bottom
                    PageNumberDecoration(pageNumber = pageContent.pageNumber)
                }
            }
        }
    }
}

@Composable
private fun OrnamentedSurahHeader(surah: Surah) {
    val frameColor = Color(0xFF8B7355)
    val goldColor = Color(0xFFD4AF37)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // Outer decorative frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFF5E6D3),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(2.dp, frameColor, RoundedCornerShape(16.dp))
                .drawBehind {
                    // Left ornament
                    drawCircle(
                        color = goldColor,
                        radius = 8.dp.toPx(),
                        center = Offset(16.dp.toPx(), size.height / 2)
                    )
                    drawCircle(
                        color = frameColor,
                        radius = 8.dp.toPx(),
                        center = Offset(16.dp.toPx(), size.height / 2),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Right ornament
                    drawCircle(
                        color = goldColor,
                        radius = 8.dp.toPx(),
                        center = Offset(size.width - 16.dp.toPx(), size.height / 2)
                    )
                    drawCircle(
                        color = frameColor,
                        radius = 8.dp.toPx(),
                        center = Offset(size.width - 16.dp.toPx(), size.height / 2),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                .padding(vertical = 12.dp, horizontal = 32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Surah name
                Text(
                    text = "سُورَةُ ${surah.name}",
                    style = QuranTextStyles.surahHeaderText.copy(
                        fontSize = 22.sp,
                        color = Color(0xFF2D1810)
                    ),
                    textAlign = TextAlign.Center
                )

                // Revelation type and verse count
                Text(
                    text = "${if (surah.revelationType == "Meccan") "مَكِّيَّة" else "مَدَنِيَّة"} - ${surah.numberOfAyahs} آية",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF5D4E37),
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TraditionalBismillah() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            style = QuranTextStyles.mushafText.copy(
                fontSize = 26.sp,
                color = Color(0xFF2D1810),
                fontWeight = FontWeight.Normal
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FlowingQuranText(
    ayahs: List<AyahWithSurah>,
    modifier: Modifier = Modifier
) {
    val verseMarkerColor = Color(0xFF8B7355)

    // Build flowing text with all ayahs concatenated
    val annotatedText = buildAnnotatedString {
        var lastSurahNumber = -1
        var skipFirstAyahOfNewSurah = false

        ayahs.forEachIndexed { index, ayahWithSurah ->
            val isNewSurah = ayahWithSurah.ayah.numberInSurah == 1 &&
                             ayahWithSurah.surah.number != lastSurahNumber

            if (isNewSurah) {
                lastSurahNumber = ayahWithSurah.surah.number
                skipFirstAyahOfNewSurah = false

                // Don't add separator if this is the first ayah on the page
                if (index > 0) {
                    append("\n\n")
                }
            }

            // Add ayah text
            withStyle(
                style = SpanStyle(
                    color = Color(0xFF2D1810),
                    fontSize = 24.sp
                )
            ) {
                append(ayahWithSurah.ayah.text)
            }

            // Add verse number marker
            withStyle(
                style = SpanStyle(
                    color = verseMarkerColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(" ﴿${convertToArabicNumerals(ayahWithSurah.ayah.numberInSurah)}﴾ ")
            }
        }
    }

    Text(
        text = annotatedText,
        style = QuranTextStyles.mushafText,
        textAlign = TextAlign.Justify,
        modifier = modifier
    )
}

@Composable
private fun PageNumberDecoration(pageNumber: Int) {
    val frameColor = Color(0xFF8B7355)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Left decorative line
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(frameColor.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Page number
            Text(
                text = convertToArabicNumerals(pageNumber),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = frameColor,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Right decorative line
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(frameColor.copy(alpha = 0.5f))
            )
        }
    }
}

private fun convertToArabicNumerals(number: Int): String {
    val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return number.toString().map { arabicNumerals[it - '0'] }.joinToString("")
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
            IconButton(
                onClick = onNextPage,
                enabled = currentPage < totalPages
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "الصفحة التالية"
                )
            }

            TextButton(
                onClick = {
                    pageInputText = currentPage.toString()
                    showPagePicker = true
                }
            ) {
                Text(
                    text = "${convertToArabicNumerals(currentPage)} / ${convertToArabicNumerals(totalPages)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

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
