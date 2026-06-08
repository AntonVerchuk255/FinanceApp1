package com.example.financeapp.instrumented_tests.dao

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.financeapp.data.AppDatabase
import com.example.financeapp.data.Goal
import com.example.financeapp.data.GoalDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class GoalDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: GoalDao

    @Before
    fun createDb() {
        db = androidx.room.Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.goalDao()
    }

    @After
    fun closeDb() { db.close() }

    @Test
    fun insertAndGetGoal() = runTest {
        dao.insert(Goal(name = "Ноутбук", targetAmount = 80_000_00L,
            currentAmount = 0L, deadline = null))
        val goals = dao.getAllGoals().first()
        assertEquals(1, goals.size)
        assertEquals("Ноутбук", goals[0].name)
    }

    @Test
    fun deleteGoal() = runTest {
        dao.insert(Goal(name = "Машина", targetAmount = 1_500_000_00L,
            currentAmount = 0L, deadline = null))
        val goal = dao.getAllGoals().first()[0]
        dao.delete(goal)
        assertEquals(0, dao.getAllGoals().first().size)
    }

    @Test
    fun addToGoal_incrementsCurrentAmount() = runTest {
        dao.insert(Goal(name = "Отпуск", targetAmount = 50_000_00L,
            currentAmount = 10_000_00L, deadline = null))
        val goal = dao.getAllGoals().first()[0]
        dao.addToGoal(goal.id, 5_000_00L)
        val updated = dao.getAllGoals().first()[0]
        assertEquals(15_000_00L, updated.currentAmount)
    }

    @Test
    fun addToGoal_multipleDeposits_accumulate() = runTest {
        dao.insert(Goal(name = "Телефон", targetAmount = 100_000_00L,
            currentAmount = 0L, deadline = null))
        val goal = dao.getAllGoals().first()[0]
        dao.addToGoal(goal.id, 10_000_00L)
        dao.addToGoal(goal.id, 10_000_00L)
        dao.addToGoal(goal.id, 10_000_00L)
        val updated = dao.getAllGoals().first()[0]
        assertEquals(30_000_00L, updated.currentAmount)
    }

    @Test
    fun goalsOrderedByCreatedAtDesc() = runTest {
        dao.insert(Goal(name = "Цель 1", targetAmount = 10_000_00L,
            currentAmount = 0L, deadline = null,
            createdAt = Instant.ofEpochMilli(1000L)))
        dao.insert(Goal(name = "Цель 2", targetAmount = 20_000_00L,
            currentAmount = 0L, deadline = null,
            createdAt = Instant.ofEpochMilli(2000L)))
        val goals = dao.getAllGoals().first()
        // Последняя созданная идёт первой
        assertEquals("Цель 2", goals[0].name)
    }

    @Test
    fun updateGoal_changesTargetAmount() = runTest {
        dao.insert(Goal(name = "Велосипед", targetAmount = 30_000_00L,
            currentAmount = 0L, deadline = null))
        val goal = dao.getAllGoals().first()[0]
        dao.update(goal.copy(targetAmount = 40_000_00L))
        val updated = dao.getAllGoals().first()[0]
        assertEquals(40_000_00L, updated.targetAmount)
    }
}