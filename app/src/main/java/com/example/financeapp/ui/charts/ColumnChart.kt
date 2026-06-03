package com.example.financeapp.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import java.time.Instant

data class ColumnChartModel(
    val date: Instant,
    val amount: Long
)

@Composable
fun ColumnChart(
    models: List<ColumnChartModel>,
    modifier: Modifier = Modifier
) {
    if (models.isEmpty()) return

    val barColor = Color(0xFF6200EE)
    val maxValue = models.maxOf { it.amount }.toFloat()

    Canvas(modifier = modifier.fillMaxSize()) {
        val barCount = models.size
        val spacing = size.width * 0.02f
        val barWidth = (size.width - spacing * (barCount + 1)) / barCount

        models.forEachIndexed { index, model ->
            val barHeight = (model.amount / maxValue) * size.height * 0.85f
            val x = spacing + index * (barWidth + spacing)
            val y = size.height - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }
    }
}