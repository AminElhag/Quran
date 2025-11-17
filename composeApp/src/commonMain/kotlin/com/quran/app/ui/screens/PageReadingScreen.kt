package com.quran.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.quran.app.tajweed.TajweedColors
import com.quran.app.tajweed.TajweedParser
import com.quran.app.ui.components.TajweedLegend
import com.quran.app.ui.theme.QuranTextStyles
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageReadingScreen(
    initialPage: Int = 1,
    repository: QuranRepository,
    readingPositionManager: ReadingPositionManager,
    textSizeManager: TextSizeManager,
    tajweedSettingsManager: TajweedSettingsManager,
    onBackClick: () -> Unit,
    onSurahListClick: () -> Unit
) {
    var pagesMap by remember { mutableStateOf<Map<Int, PageContent>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var totalPages by remember { mutableStateOf(604) }
    val scope = rememberCoroutineScope()

    var startPage by remember { mutableStateOf(initialPage) }

    // Text size and full page mode state
    var textSizeLevel by remember { mutableStateOf(textSizeManager.getTextSizeLevel()) }
    var isFullPageMode by remember { mutableStateOf(textSizeManager.isFullPageModeEnabled()) }
    var showControls by remember { mutableStateOf(!isFullPageMode) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    // Tajweed settings state
    var isTajweedEnabled by remember { mutableStateOf(tajweedSettingsManager.isTajweedEnabled()) }
    var showTajweedLegend by remember { mutableStateOf(false) }

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

    // Calculate text size multiplier
    val textSizeMultiplier = textSizeManager.getTextSizeMultiplier(textSizeLevel)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                AnimatedVisibility(
                    visible = showControls,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
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
                            // Tajweed toggle button
                            IconButton(
                                onClick = {
                                    isTajweedEnabled = !isTajweedEnabled
                                    tajweedSettingsManager.saveTajweedEnabled(isTajweedEnabled)
                                }
                            ) {
                                Text(
                                    text = "ت",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isTajweedEnabled) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }

                            // Tajweed legend button
                            IconButton(
                                onClick = { showTajweedLegend = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "دليل التجويد"
                                )
                            }

                            // Text size decrease button
                            IconButton(
                                onClick = {
                                    if (textSizeLevel > TextSizeManager.MIN_SIZE_LEVEL) {
                                        textSizeLevel--
                                        textSizeManager.saveTextSizeLevel(textSizeLevel)
                                    }
                                },
                                enabled = textSizeLevel > TextSizeManager.MIN_SIZE_LEVEL
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Create,
                                    contentDescription = "تصغير الخط"
                                )
                            }

                            // Text size increase button
                            IconButton(
                                onClick = {
                                    if (textSizeLevel < TextSizeManager.MAX_SIZE_LEVEL) {
                                        textSizeLevel++
                                        textSizeManager.saveTextSizeLevel(textSizeLevel)
                                    }
                                },
                                enabled = textSizeLevel < TextSizeManager.MAX_SIZE_LEVEL
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "تكبير الخط"
                                )
                            }

                            // Full page mode toggle
                            IconButton(
                                onClick = {
                                    isFullPageMode = !isFullPageMode
                                    textSizeManager.saveFullPageMode(isFullPageMode)
                                    if (isFullPageMode) {
                                        showControls = false
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isFullPageMode) Icons.Default.ShoppingCart else Icons.Default.Call,
                                    contentDescription = if (isFullPageMode) "إنهاء وضع الشاشة الكاملة" else "وضع الشاشة الكاملة"
                                )
                            }

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
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = showControls,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
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
            }
        ) { paddingValues ->
            val contentPadding = if (showControls) paddingValues else PaddingValues(0.dp)

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
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
                        .padding(contentPadding)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (isFullPageMode) {
                                showControls = !showControls
                            }
                        },
                    reverseLayout = true,
                    key = { it }
                ) { pageIndex ->
                    val pageNumber = pageIndex + 1
                    val pageContent = pagesMap[pageNumber]

                    if (pageContent != null) {
                        TraditionalQuranPage(
                            pageContent = pageContent,
                            textSizeMultiplier = textSizeMultiplier,
                            isTajweedEnabled = isTajweedEnabled,
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

        // Text size indicator (shown briefly when changed)
        AnimatedVisibility(
            visible = showControls && !isLoading,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp
            ) {
                Text(
                    text = "حجم الخط: $textSizeLevel",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Tajweed legend dialog
        if (showTajweedLegend) {
            AlertDialog(
                onDismissRequest = { showTajweedLegend = false },
                confirmButton = {},
                text = {
                    TajweedLegend(
                        onDismiss = { showTajweedLegend = false }
                    )
                }
            )
        }
    }
}

@Composable
private fun TraditionalQuranPage(
    pageContent: PageContent,
    textSizeMultiplier: Float = 1.0f,
    isTajweedEnabled: Boolean = true,
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
                            OrnamentedSurahHeader(
                                surah = ayahWithSurah.surah,
                                textSizeMultiplier = textSizeMultiplier
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Bismillah (except for At-Tawbah and Al-Fatiha)
                            if (ayahWithSurah.surah.number != 9 && ayahWithSurah.surah.number != 1) {
                                TraditionalBismillah(textSizeMultiplier = textSizeMultiplier)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    // Flowing Quranic text
                    FlowingQuranText(
                        ayahs = pageContent.ayahs,
                        textSizeMultiplier = textSizeMultiplier,
                        isTajweedEnabled = isTajweedEnabled,
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
private fun OrnamentedSurahHeader(
    surah: Surah,
    textSizeMultiplier: Float = 1.0f
) {
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
                        fontSize = (22 * textSizeMultiplier).sp,
                        color = Color(0xFF2D1810)
                    ),
                    textAlign = TextAlign.Center
                )

                // Revelation type and verse count
                Text(
                    text = "${if (surah.revelationType == "Meccan") "مَكِّيَّة" else "مَدَنِيَّة"} - ${surah.numberOfAyahs} آية",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF5D4E37),
                        fontSize = (12 * textSizeMultiplier).sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TraditionalBismillah(textSizeMultiplier: Float = 1.0f) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            style = QuranTextStyles.mushafText.copy(
                fontSize = (26 * textSizeMultiplier).sp,
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
    textSizeMultiplier: Float = 1.0f,
    isTajweedEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val verseMarkerColor = Color(0xFF8B7355)
    val baseFontSize = 24 * textSizeMultiplier
    val markerFontSize = 20 * textSizeMultiplier
    val defaultTextColor = Color(0xFF2D1810)

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

            // Add ayah text with Tajweed styling if enabled
            if (isTajweedEnabled) {
                // Parse and apply Tajweed colors
                val tajweedSegments = TajweedParser.autoDetectTajweed(ayahWithSurah.ayah.text)
                for (segment in tajweedSegments) {
                    val color = if (segment.rule == com.quran.app.tajweed.TajweedRule.DEFAULT) {
                        defaultTextColor
                    } else {
                        com.quran.app.tajweed.getTajweedColor(segment.rule)
                    }
                    withStyle(
                        style = SpanStyle(
                            color = color,
                            fontSize = baseFontSize.sp
                        )
                    ) {
                        append(segment.text)
                    }
                }
            } else {
                // Plain text without Tajweed styling
                withStyle(
                    style = SpanStyle(
                        color = defaultTextColor,
                        fontSize = baseFontSize.sp
                    )
                ) {
                    append(ayahWithSurah.ayah.text)
                }
            }

            // Add verse number marker
            withStyle(
                style = SpanStyle(
                    color = verseMarkerColor,
                    fontSize = markerFontSize.sp,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(" ﴿${convertToArabicNumerals(ayahWithSurah.ayah.numberInSurah)}﴾ ")
            }
        }
    }

    Text(
        text = annotatedText,
        style = QuranTextStyles.mushafText.copy(
            lineHeight = (48 * textSizeMultiplier).sp
        ),
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
