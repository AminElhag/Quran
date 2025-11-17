package com.quran.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quran.app.tajweed.TajweedColors
import com.quran.app.tajweed.TajweedRule
import com.quran.app.tajweed.getTajweedColor

/**
 * Displays a legend explaining Tajweed color coding
 */
@Composable
fun TajweedLegend(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "دليل ألوان التجويد",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Legend items in a grid-like layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TajweedLegendItem(
                    color = TajweedColors.ghunnah,
                    arabicName = "غنة",
                    description = "النون والميم المشددتان"
                )

                TajweedLegendItem(
                    color = TajweedColors.ikhfa,
                    arabicName = "إخفاء",
                    description = "إخفاء النون الساكنة والتنوين"
                )

                TajweedLegendItem(
                    color = TajweedColors.idgham,
                    arabicName = "إدغام",
                    description = "إدغام النون الساكنة والتنوين"
                )

                TajweedLegendItem(
                    color = TajweedColors.iqlab,
                    arabicName = "إقلاب",
                    description = "قلب النون إلى ميم عند الباء"
                )

                TajweedLegendItem(
                    color = TajweedColors.qalqalah,
                    arabicName = "قلقلة",
                    description = "حروف القلقلة (ق ط ب ج د)"
                )

                TajweedLegendItem(
                    color = TajweedColors.madd,
                    arabicName = "مد",
                    description = "المد الطبيعي والفرعي"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("إغلاق")
            }
        }
    }
}

@Composable
private fun TajweedLegendItem(
    color: androidx.compose.ui.graphics.Color,
    arabicName: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // Description
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = arabicName,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                textAlign = TextAlign.End
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Color indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color, CircleShape)
        )
    }
}

/**
 * Compact version of the legend for toolbar display
 */
@Composable
fun CompactTajweedLegend(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            TajweedColors.ghunnah to "غ",
            TajweedColors.ikhfa to "خ",
            TajweedColors.idgham to "د",
            TajweedColors.iqlab to "ق",
            TajweedColors.qalqalah to "ل",
            TajweedColors.madd to "م"
        ).forEach { (color, label) ->
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}
