package com.example.financeapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 5): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC, createdAt DESC")
    fun getTransactionsByPeriod(start: Instant, end: Instant): Flow<List<Transaction>>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = :type AND date BETWEEN :start AND :end
    """)
    fun getSumByTypeAndPeriod(
        type: TransactionType,
        start: Instant,
        end: Instant
    ): Flow<Long?>

    @Query("""
        SELECT c.name, SUM(t.amount) as total 
        FROM transactions t 
        JOIN categories c ON t.categoryId = c.id 
        WHERE t.type = :type AND t.date BETWEEN :start AND :end 
        GROUP BY t.categoryId ORDER BY total DESC
    """)
    fun getCategoryDistributionByPeriod(
        type: TransactionType,
        start: Instant,
        end: Instant
    ): Flow<List<CategorySum>>

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE type = 'INCOME'
    """)
    fun getTotalIncome(): Flow<Long?>

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE type = 'EXPENSE'
    """)
    fun getTotalExpense(): Flow<Long?>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsOnce(): List<Transaction>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("""
    SELECT strftime('%Y-%m', datetime(date/1000, 'unixepoch')) as month,
    SUM(amount) as total
    FROM transactions
    WHERE type = :type
    GROUP BY month
    ORDER BY month DESC
    LIMIT 12
""")
    fun getMonthlyTotals(type: TransactionType): Flow<List<MonthlyTotal>>

    @Query("""
    SELECT c.name, SUM(t.amount) as total
    FROM transactions t
    JOIN categories c ON t.categoryId = c.id
    WHERE t.type = :type AND t.date BETWEEN :start AND :end
    GROUP BY t.categoryId ORDER BY total DESC
""")
    suspend fun getCategoryTotalsOnce(
        type: TransactionType,
        start: Instant,
        end: Instant
    ): List<CategorySum>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :start AND :end")
    suspend fun getSumByTypeAndPeriodOnce(
        type: TransactionType,
        start: Instant,
        end: Instant
    ): Long?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    suspend fun getTotalIncomeOnce(): Long?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    suspend fun getTotalExpenseOnce(): Long?

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC LIMIT :limit")
    suspend fun getRecentTransactionsOnce(limit: Int): List<Transaction>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC, createdAt DESC")
    suspend fun getTransactionsByPeriodOnce(start: Instant, end: Instant): List<Transaction>

}