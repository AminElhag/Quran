package com.quran.app.tajweed

import androidx.compose.ui.graphics.Color

/**
 * Tajweed color scheme following standard Islamic color coding conventions.
 * These colors are used to highlight different pronunciation rules in the Quran.
 */
object TajweedColors {
    // Ghunnah (Nasalization) - Shows where to make nasal sounds
    val ghunnah = Color(0xFF4CAF50) // Green

    // Ikhfa (Hiding) - Letters that require hiding the noon sakinah/tanween
    val ikhfa = Color(0xFFE91E63) // Pink/Magenta

    // Idgham (Merging) - Letters where sounds merge together
    val idgham = Color(0xFFFF9800) // Orange

    // Iqlab (Conversion) - Noon converted to Meem before Ba
    val iqlab = Color(0xFF2196F3) // Blue

    // Qalqalah (Echo/Bouncing) - Letters that bounce (ق ط ب ج د)
    val qalqalah = Color(0xFF9C27B0) // Purple

    // Madd (Prolongation) - Extended vowel sounds
    val madd = Color(0xFFD32F2F) // Red

    // Lam Shamsiyyah (Solar Lam) - Silent Lam before sun letters
    val lamShamsiyyah = Color(0xFF795548) // Brown

    // Silent letters / Small letters
    val silent = Color(0xFF607D8B) // Gray-Blue

    // Special Waqf (Stop) marks
    val waqf = Color(0xFF00BCD4) // Cyan

    // Thick letters (Tafkheem)
    val thick = Color(0xFF8BC34A) // Light Green

    // Default text color (no special Tajweed rule)
    val defaultText = Color(0xFF2D1810) // Dark brown
}

/**
 * Tajweed rule types corresponding to different pronunciation rules
 */
enum class TajweedRule {
    GHUNNAH,        // غنة - Nasalization
    IKHFA,          // إخفاء - Hiding
    IDGHAM,         // إدغام - Merging
    IQLAB,          // إقلاب - Conversion
    QALQALAH,       // قلقلة - Echo/Bouncing
    MADD_NORMAL,    // مد طبيعي - Normal prolongation (2 counts)
    MADD_MUNFASIL,  // مد منفصل - Separated prolongation (4-5 counts)
    MADD_MUTTASIL,  // مد متصل - Connected prolongation (4-5 counts)
    MADD_LAZIM,     // مد لازم - Obligatory prolongation (6 counts)
    MADD_ARID,      // مد عارض - Presented prolongation
    LAM_SHAMSIYYAH, // لام شمسية - Solar Lam (silent)
    SILENT,         // حروف لا تنطق - Silent letters
    THICK,          // تفخيم - Thick/Emphatic pronunciation
    DEFAULT         // Default text
}

/**
 * Maps Tajweed rules to their corresponding colors
 */
fun getTajweedColor(rule: TajweedRule): Color {
    return when (rule) {
        TajweedRule.GHUNNAH -> TajweedColors.ghunnah
        TajweedRule.IKHFA -> TajweedColors.ikhfa
        TajweedRule.IDGHAM -> TajweedColors.idgham
        TajweedRule.IQLAB -> TajweedColors.iqlab
        TajweedRule.QALQALAH -> TajweedColors.qalqalah
        TajweedRule.MADD_NORMAL,
        TajweedRule.MADD_MUNFASIL,
        TajweedRule.MADD_MUTTASIL,
        TajweedRule.MADD_LAZIM,
        TajweedRule.MADD_ARID -> TajweedColors.madd
        TajweedRule.LAM_SHAMSIYYAH -> TajweedColors.lamShamsiyyah
        TajweedRule.SILENT -> TajweedColors.silent
        TajweedRule.THICK -> TajweedColors.thick
        TajweedRule.DEFAULT -> TajweedColors.defaultText
    }
}
