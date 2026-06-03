package com.example.financeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Long,     // цель в копейках
    val currentAmount: Long = 0, // накоплено
    val deadline: Instant?,
    val colorHex: String = "#6200EE",
    val createdAt: Instant = Instant.now()
)