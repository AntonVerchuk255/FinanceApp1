package com.example.financeapp.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

@Serializable
data class BackupData(
    val transactions: List<TransactionBackup>,
    val categories: List<CategoryBackup>,
    val budgets: List<BudgetBackup>,
    val goals: List<GoalBackup>,
    val exportedAt: Long = System.currentTimeMillis()
)

@Serializable
data class TransactionBackup(
    val id: Long,
    val type: String,
    val amount: Long,
    val categoryId: Long?,
    val date: Long,
    val note: String?,
    val createdAt: Long
)

@Serializable
data class CategoryBackup(
    val id: Long,
    val name: String,
    val type: String,
    val iconRes: Int?,
    val colorHex: String
)

@Serializable
data class BudgetBackup(
    val id: Long,
    val categoryId: Long,
    val limitAmount: Long,
    val periodType: String
)

@Serializable
data class GoalBackup(
    val id: Long,
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long,
    val deadline: Long?,
    val colorHex: String,
    val createdAt: Long
)

class BackupManager(private val db: AppDatabase) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun exportBackup(context: Context, uri: Uri): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val transactions = db.transactionDao().getAllTransactionsOnce()
                val categories = db.categoryDao().getAllCategoriesOnce()
                val budgets = db.budgetDao().getAllBudgetsOnce()
                val goals = db.goalDao().getAllGoalsOnce()

                val backup = BackupData(
                    transactions = transactions.map {
                        TransactionBackup(
                            id = it.id,
                            type = it.type.name,
                            amount = it.amount,
                            categoryId = it.categoryId,
                            date = it.date.toEpochMilli(),
                            note = it.note,
                            createdAt = it.createdAt.toEpochMilli()
                        )
                    },
                    categories = categories.map {
                        CategoryBackup(
                            id = it.id,
                            name = it.name,
                            type = it.type.name,
                            iconRes = it.iconRes,
                            colorHex = it.colorHex
                        )
                    },
                    budgets = budgets.map {
                        BudgetBackup(
                            id = it.id,
                            categoryId = it.categoryId,
                            limitAmount = it.limitAmount,
                            periodType = it.periodType.name
                        )
                    },
                    goals = goals.map {
                        GoalBackup(
                            id = it.id,
                            name = it.name,
                            targetAmount = it.targetAmount,
                            currentAmount = it.currentAmount,
                            deadline = it.deadline?.toEpochMilli(),
                            colorHex = it.colorHex,
                            createdAt = it.createdAt.toEpochMilli()
                        )
                    }
                )

                val jsonString = json.encodeToString(backup)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(jsonString.toByteArray())
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun importBackup(context: Context, uri: Uri): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                } ?: return@withContext Result.failure(Exception("Не удалось открыть файл"))

                val backup = json.decodeFromString<BackupData>(jsonString)

                // Очищаем и восстанавливаем
                db.categoryDao().deleteAll()
                db.transactionDao().deleteAll()
                db.budgetDao().deleteAll()
                db.goalDao().deleteAll()

                backup.categories.forEach {
                    db.categoryDao().insert(Category(
                        id = it.id,
                        name = it.name,
                        type = TransactionType.valueOf(it.type),
                        iconRes = it.iconRes,
                        colorHex = it.colorHex
                    ))
                }
                backup.transactions.forEach {
                    db.transactionDao().insert(Transaction(
                        id = it.id,
                        type = TransactionType.valueOf(it.type),
                        amount = it.amount,
                        categoryId = it.categoryId,
                        date = Instant.ofEpochMilli(it.date),
                        note = it.note,
                        createdAt = Instant.ofEpochMilli(it.createdAt)
                    ))
                }
                backup.budgets.forEach {
                    db.budgetDao().insert(Budget(
                        id = it.id,
                        categoryId = it.categoryId,
                        limitAmount = it.limitAmount,
                        periodType = PeriodType.valueOf(it.periodType)
                    ))
                }
                backup.goals.forEach {
                    db.goalDao().insert(Goal(
                        id = it.id,
                        name = it.name,
                        targetAmount = it.targetAmount,
                        currentAmount = it.currentAmount,
                        deadline = it.deadline?.let { d -> Instant.ofEpochMilli(d) },
                        colorHex = it.colorHex,
                        createdAt = Instant.ofEpochMilli(it.createdAt)
                    ))
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}