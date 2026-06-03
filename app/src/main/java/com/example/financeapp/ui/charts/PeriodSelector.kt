package com.example.financeapp.ui.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.temporal.ChronoUnit

enum class Period(val label: String) {
    WEEK("Неделя"),
    MONTH("Месяц"),
    YEAR("Год")
}

@Composable
fun PeriodSelector(onRangeSelected: (Instant, Instant) -> Unit) {
    var selected by remember { mutableStateOf(Period.MONTH) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Period.entries.forEach { period ->
            FilterChip(
                selected = selected == period,
                onClick = {
                    selected = period
                    val now = Instant.now()
                    val start = when (period) {
                        Period.WEEK -> now.minus(7, ChronoUnit.DAYS)
                        Period.MONTH -> now.minus(30, ChronoUnit.DAYS)
                        Period.YEAR -> now.minus(365, ChronoUnit.DAYS)
                    }
                    onRangeSelected(start, now)
                },
                label = { Text(period.label) }
            )
        }
    }
}