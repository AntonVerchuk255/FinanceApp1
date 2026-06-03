package com.example.financeapp.ui.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.data.CategorySum

val categoryColors = listOf(
    Color(0xFF6200EE), Color(0xFF03DAC5), Color(0xFFFF6D00),
    Color(0xFF00C853), Color(0xFFD50000), Color(0xFF2979FF),
    Color(0xFFFFD600), Color(0xFFAA00FF)
)

@Composable
fun ChartLegend(categories: List<CategorySum>) {
    val total = categories.sumOf { it.total }.toFloat()

    Column(modifier = Modifier.fillMaxWidth()) {
        categories.take(8).forEachIndexed { index, cat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(categoryColors[index % categoryColors.size])
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = cat.name, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "%.2f ₽".format(cat.total / 100.0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "%.1f%%".format(if (total > 0) cat.total / total * 100 else 0f),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (index < categories.size - 1) {
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}