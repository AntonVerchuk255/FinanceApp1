package com.example.financeapp.data

import androidx.room.ColumnInfo

data class MonthlyTotal(
    val month: String,
    @ColumnInfo(name = "total") val total: Long
)