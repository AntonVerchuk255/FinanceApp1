package com.example.financeapp.unit_tests.viewmodel

import com.example.financeapp.data.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class ModelsTest {

    @Test
    fun testBudget() {
        val budget = Budget(
            id = 1,
            categoryId = 10,
            limitAmount = 50_000L,
            periodType = PeriodType.MONTHLY
        )

        assertEquals(1, budget.id)
        assertEquals(10, budget.categoryId)
        assertEquals(50_000L, budget.limitAmount)
        assertEquals(PeriodType.MONTHLY, budget.periodType)
    }

    @Test
    fun testCategory() {
        val category = Category(
            id = 1,
            name = "Еда",
            type = TransactionType.EXPENSE,
            iconRes = null,
            colorHex = "#FF0000"
        )

        assertEquals(1, category.id)
        assertEquals("Еда", category.name)
        assertEquals(TransactionType.EXPENSE, category.type)
        assertEquals("#FF0000", category.colorHex)
    }

    @Test
    fun testCategorySum() {
        val categorySum = CategorySum("Еда", 10_000L)

        assertEquals("Еда", categorySum.name)
        assertEquals(10_000L, categorySum.total)
    }

    @Test
    fun testGoal() {
        val deadline = Instant.now()
        val goal = Goal(
            id = 1,
            name = "Ноутбук",
            targetAmount = 100_000L,
            currentAmount = 50_000L,
            deadline = deadline,
            colorHex = "#6200EE"
        )

        assertEquals(1, goal.id)
        assertEquals("Ноутбук", goal.name)
        assertEquals(100_000L, goal.targetAmount)
        assertEquals(50_000L, goal.currentAmount)
        assertEquals(deadline, goal.deadline)
        assertEquals("#6200EE", goal.colorHex)
    }

    @Test
    fun testMonthlyTotal() {
        val monthlyTotal = MonthlyTotal("2024-01", 15_000L)

        assertEquals("2024-01", monthlyTotal.month)
        assertEquals(15_000L, monthlyTotal.total)
    }

    @Test
    fun testReminder() {
        val reminder = Reminder(
            id = 1,
            title = "Аренда",
            amount = 30_000L,
            categoryId = 5L,
            dayOfMonth = 5,
            isActive = true
        )

        assertEquals(1, reminder.id)
        assertEquals("Аренда", reminder.title)
        assertEquals(30_000L, reminder.amount)
        assertEquals(5L, reminder.categoryId)
        assertEquals(5, reminder.dayOfMonth)
        assertTrue(reminder.isActive)
    }

    @Test
    fun testTransaction() {
        val date = Instant.now()
        val transaction = Transaction(
            id = 1,
            type = TransactionType.EXPENSE,
            amount = 1_500L,
            categoryId = 3L,
            date = date,
            note = "Обед"
        )

        assertEquals(1, transaction.id)
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(1_500L, transaction.amount)
        assertEquals(3L, transaction.categoryId)
        assertEquals(date, transaction.date)
        assertEquals("Обед", transaction.note)
    }

    @Test
    fun testTransactionType() {
        assertEquals("INCOME", TransactionType.INCOME.name)
        assertEquals("EXPENSE", TransactionType.EXPENSE.name)
        assertEquals("TRANSFER", TransactionType.TRANSFER.name)
    }

    @Test
    fun testPeriodType() {
        assertEquals("WEEKLY", PeriodType.WEEKLY.name)
        assertEquals("MONTHLY", PeriodType.MONTHLY.name)
    }
}