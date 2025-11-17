package com.quran.app.tajweed

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit

/**
 * Represents a segment of text with its associated Tajweed rule
 */
data class TajweedSegment(
    val text: String,
    val rule: TajweedRule
)

/**
 * Parser for Tajweed-encoded Quran text.
 *
 * Supports multiple formats:
 * 1. HTML-style tags: <tajweed class="ghunnah">text</tajweed>
 * 2. Span-style tags: <span class="tajweed-ghunnah">text</span>
 * 3. Custom markers: [ghunnah]text[/ghunnah]
 * 4. Unicode-based automatic detection for basic rules
 */
object TajweedParser {

    // Qalqalah letters (ق ط ب ج د)
    private val qalqalahLetters = setOf('ق', 'ط', 'ب', 'ج', 'د')

    // Sukoon mark
    private const val SUKOON = '\u0652'

    // Tanween marks
    private const val FATHATAN = '\u064B'
    private const val DAMMATAN = '\u064C'
    private const val KASRATAN = '\u064D'

    // Noon and Meem
    private const val NOON = 'ن'
    private const val MEEM = 'م'

    // Ikhfa letters (ت ث ج د ذ ز س ش ص ض ط ظ ف ق ك)
    private val ikhfaLetters = setOf('ت', 'ث', 'ج', 'د', 'ذ', 'ز', 'س', 'ش', 'ص', 'ض', 'ط', 'ظ', 'ف', 'ق', 'ك')

    // Idgham letters with Ghunnah (ي ن م و)
    private val idghamGhunnahLetters = setOf('ي', 'ن', 'م', 'و')

    // Idgham letters without Ghunnah (ل ر)
    private val idghamNoGhunnahLetters = setOf('ل', 'ر')

    // Iqlab letter (ب)
    private const val BA = 'ب'

    // Madd letters (ا و ي)
    private val maddLetters = setOf('ا', 'و', 'ي', 'ى')

    // Small Alef (special madd character)
    private const val SMALL_ALEF = '\u0670'

    // Shadda
    private const val SHADDA = '\u0651'

    // Madda above (آ)
    private const val MADDA = '\u0653'

    /**
     * Parse HTML-tagged Tajweed text into segments
     * Format: <tajweed class="rule">text</tajweed> or <span class="rule">text</span>
     */
    fun parseHtmlTaggedText(text: String): List<TajweedSegment> {
        val segments = mutableListOf<TajweedSegment>()
        var currentIndex = 0

        // Regex to match Tajweed tags
        val tagPattern = Regex("""<(tajweed|span)\s+class="([^"]+)">(.*?)</\1>""", RegexOption.DOT_MATCHES_ALL)

        var lastEnd = 0
        for (match in tagPattern.findAll(text)) {
            // Add any plain text before this tag
            if (match.range.first > lastEnd) {
                val plainText = text.substring(lastEnd, match.range.first)
                if (plainText.isNotEmpty()) {
                    segments.add(TajweedSegment(plainText, TajweedRule.DEFAULT))
                }
            }

            // Extract the rule and text from the tag
            val ruleClass = match.groupValues[2]
            val taggedText = match.groupValues[3]

            val rule = classNameToRule(ruleClass)
            segments.add(TajweedSegment(taggedText, rule))

            lastEnd = match.range.last + 1
        }

        // Add any remaining plain text
        if (lastEnd < text.length) {
            val remainingText = text.substring(lastEnd)
            if (remainingText.isNotEmpty()) {
                segments.add(TajweedSegment(remainingText, TajweedRule.DEFAULT))
            }
        }

        return segments
    }

    /**
     * Parse custom bracket-style Tajweed markers
     * Format: [rule]text[/rule]
     */
    fun parseBracketTaggedText(text: String): List<TajweedSegment> {
        val segments = mutableListOf<TajweedSegment>()

        val tagPattern = Regex("""\[([a-z_]+)\](.*?)\[/\1\]""", RegexOption.DOT_MATCHES_ALL)

        var lastEnd = 0
        for (match in tagPattern.findAll(text)) {
            if (match.range.first > lastEnd) {
                val plainText = text.substring(lastEnd, match.range.first)
                if (plainText.isNotEmpty()) {
                    segments.add(TajweedSegment(plainText, TajweedRule.DEFAULT))
                }
            }

            val ruleName = match.groupValues[1]
            val taggedText = match.groupValues[2]

            val rule = classNameToRule(ruleName)
            segments.add(TajweedSegment(taggedText, rule))

            lastEnd = match.range.last + 1
        }

        if (lastEnd < text.length) {
            val remainingText = text.substring(lastEnd)
            if (remainingText.isNotEmpty()) {
                segments.add(TajweedSegment(remainingText, TajweedRule.DEFAULT))
            }
        }

        return segments
    }

    /**
     * Automatically detect Tajweed rules based on Arabic text patterns.
     * This is a simplified detection and may not be 100% accurate.
     * For best results, use pre-tagged Tajweed text.
     */
    fun autoDetectTajweed(text: String): List<TajweedSegment> {
        val segments = mutableListOf<TajweedSegment>()
        val chars = text.toList()
        var i = 0

        while (i < chars.size) {
            val char = chars[i]
            var rule = TajweedRule.DEFAULT
            var segmentLength = 1

            // Check for Qalqalah (letter with sukoon)
            if (qalqalahLetters.contains(char) && i + 1 < chars.size && chars[i + 1] == SUKOON) {
                rule = TajweedRule.QALQALAH
                segmentLength = 2
            }
            // Check for Ghunnah (Noon or Meem with Shadda)
            else if ((char == NOON || char == MEEM) && i + 1 < chars.size && chars[i + 1] == SHADDA) {
                rule = TajweedRule.GHUNNAH
                segmentLength = 2
            }
            // Check for Noon Sakinah/Tanween followed by Ikhfa letters
            else if (char == NOON && i + 1 < chars.size && chars[i + 1] == SUKOON) {
                // Look ahead for Ikhfa, Idgham, or Iqlab
                var nextLetterIndex = i + 2
                while (nextLetterIndex < chars.size && !chars[nextLetterIndex].isArabicLetter()) {
                    nextLetterIndex++
                }
                if (nextLetterIndex < chars.size) {
                    val nextLetter = chars[nextLetterIndex]
                    when {
                        ikhfaLetters.contains(nextLetter) -> {
                            rule = TajweedRule.IKHFA
                            segmentLength = 2
                        }
                        idghamGhunnahLetters.contains(nextLetter) || idghamNoGhunnahLetters.contains(nextLetter) -> {
                            rule = TajweedRule.IDGHAM
                            segmentLength = 2
                        }
                        nextLetter == BA -> {
                            rule = TajweedRule.IQLAB
                            segmentLength = 2
                        }
                    }
                }
            }
            // Check for Tanween followed by specific letters
            else if (char == FATHATAN || char == DAMMATAN || char == KASRATAN) {
                var nextLetterIndex = i + 1
                while (nextLetterIndex < chars.size && !chars[nextLetterIndex].isArabicLetter()) {
                    nextLetterIndex++
                }
                if (nextLetterIndex < chars.size) {
                    val nextLetter = chars[nextLetterIndex]
                    when {
                        ikhfaLetters.contains(nextLetter) -> rule = TajweedRule.IKHFA
                        idghamGhunnahLetters.contains(nextLetter) || idghamNoGhunnahLetters.contains(nextLetter) -> rule = TajweedRule.IDGHAM
                        nextLetter == BA -> rule = TajweedRule.IQLAB
                    }
                }
            }
            // Check for Madd (prolongation)
            else if (maddLetters.contains(char) || char == SMALL_ALEF || char == MADDA) {
                rule = TajweedRule.MADD_NORMAL
            }

            // Add segment
            val segmentText = chars.subList(i, minOf(i + segmentLength, chars.size)).joinToString("")

            // Try to merge with previous segment if same rule
            if (segments.isNotEmpty() && segments.last().rule == rule) {
                val lastSegment = segments.removeAt(segments.size - 1)
                segments.add(TajweedSegment(lastSegment.text + segmentText, rule))
            } else {
                segments.add(TajweedSegment(segmentText, rule))
            }

            i += segmentLength
        }

        return segments
    }

    /**
     * Convert Tajweed segments to Compose AnnotatedString with colored styling
     */
    fun buildAnnotatedString(
        segments: List<TajweedSegment>,
        fontSize: TextUnit,
        defaultColor: Color = TajweedColors.defaultText
    ): AnnotatedString {
        return buildAnnotatedString {
            for (segment in segments) {
                val color = if (segment.rule == TajweedRule.DEFAULT) {
                    defaultColor
                } else {
                    getTajweedColor(segment.rule)
                }

                withStyle(
                    style = SpanStyle(
                        color = color,
                        fontSize = fontSize
                    )
                ) {
                    append(segment.text)
                }
            }
        }
    }

    /**
     * Convenience method to parse and build annotated string in one step
     */
    fun parseAndBuildAnnotatedString(
        text: String,
        fontSize: TextUnit,
        defaultColor: Color = TajweedColors.defaultText,
        enableAutoDetection: Boolean = true
    ): AnnotatedString {
        val segments = if (text.contains("<tajweed") || text.contains("<span")) {
            parseHtmlTaggedText(text)
        } else if (text.contains("[") && text.contains("[/")) {
            parseBracketTaggedText(text)
        } else if (enableAutoDetection) {
            autoDetectTajweed(text)
        } else {
            listOf(TajweedSegment(text, TajweedRule.DEFAULT))
        }

        return buildAnnotatedString(segments, fontSize, defaultColor)
    }

    /**
     * Maps CSS class names to Tajweed rules
     */
    private fun classNameToRule(className: String): TajweedRule {
        return when {
            className.contains("ghunnah", ignoreCase = true) -> TajweedRule.GHUNNAH
            className.contains("ikhfa", ignoreCase = true) -> TajweedRule.IKHFA
            className.contains("idgham", ignoreCase = true) -> TajweedRule.IDGHAM
            className.contains("iqlab", ignoreCase = true) -> TajweedRule.IQLAB
            className.contains("qalqalah", ignoreCase = true) ||
            className.contains("qalqlah", ignoreCase = true) -> TajweedRule.QALQALAH
            className.contains("madd") || className.contains("medd") -> {
                when {
                    className.contains("lazim", ignoreCase = true) -> TajweedRule.MADD_LAZIM
                    className.contains("muttasil", ignoreCase = true) ||
                    className.contains("munfasil", ignoreCase = true) -> TajweedRule.MADD_MUTTASIL
                    className.contains("arid", ignoreCase = true) -> TajweedRule.MADD_ARID
                    else -> TajweedRule.MADD_NORMAL
                }
            }
            className.contains("lam_shamsiyyah", ignoreCase = true) ||
            className.contains("shamsi", ignoreCase = true) -> TajweedRule.LAM_SHAMSIYYAH
            className.contains("silent", ignoreCase = true) -> TajweedRule.SILENT
            className.contains("thick", ignoreCase = true) ||
            className.contains("tafkheem", ignoreCase = true) -> TajweedRule.THICK
            else -> TajweedRule.DEFAULT
        }
    }

    /**
     * Check if a character is an Arabic letter (not a diacritic)
     */
    private fun Char.isArabicLetter(): Boolean {
        // Arabic letters range: U+0621 to U+063A and U+0641 to U+064A
        val code = this.code
        return (code in 0x0621..0x063A) || (code in 0x0641..0x064A)
    }
}
