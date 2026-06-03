package com.example.financeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

enum class TransactionType { INCOME, EXPENSE, TRANSFER }

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Long,
    val categoryId: Long?,          // null для переводов
    val date: Instant,
    val note: String?,
    val createdAt: Instant = Instant.now()
)