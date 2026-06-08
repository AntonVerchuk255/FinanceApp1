package com.example.financeapp.unit_tests.viewmodel

import com.example.financeapp.data.Goal
import com.example.financeapp.data.GoalDao
import com.example.financeapp.ui.goals.GoalsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var goalDao: GoalDao
    private lateinit var viewModel: GoalsViewModel

    private val sampleGoals = listOf(
        Goal(id = 1, name = "Ноутбук", targetAmount = 80_000_00L,
            currentAmount = 20_000_00L, deadline = null),
        Goal(id = 2, name = "Отпуск", targetAmount = 50_000_00L,
            currentAmount = 50_000_00L, deadline = Instant.now())
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        goalDao = mockk(relaxed = true)
        every { goalDao.getAllGoals() } returns flowOf(sampleGoals)
        viewModel = GoalsViewModel(goalDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `goals are loaded from dao`() = runTest {
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.goals.size)
        job.cancel()
    }

    @Test
    fun `goal names are correct`() = runTest {
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        val names = viewModel.uiState.value.goals.map { it.name }
        assertEquals(listOf("Ноутбук", "Отпуск"), names)
        job.cancel()
    }

    @Test
    fun `addGoal calls dao insert`() = runTest {
        coEvery { goalDao.insert(any()) } just Runs
        viewModel.addGoal(
            name = "Машина",
            targetAmount = 1_500_000_00L,
            deadline = null,
            colorHex = "#FF6D00"
        )
        advanceUntilIdle()
        coVerify(exactly = 1) { goalDao.insert(any()) }
    }

    @Test
    fun `addToGoal calls dao addToGoal with correct params`() = runTest {
        coEvery { goalDao.addToGoal(1L, 5_000_00L) } just Runs
        viewModel.addToGoal(goalId = 1L, amount = 5_000_00L)
        advanceUntilIdle()
        coVerify(exactly = 1) { goalDao.addToGoal(1L, 5_000_00L) }
    }

    @Test
    fun `deleteGoal calls dao delete`() = runTest {
        val goal = sampleGoals[0]
        coEvery { goalDao.delete(goal) } just Runs
        viewModel.deleteGoal(goal)
        advanceUntilIdle()
        coVerify(exactly = 1) { goalDao.delete(goal) }
    }

    @Test
    fun `completed goal has currentAmount equal to targetAmount`() = runTest {
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        val completed = viewModel.uiState.value.goals.find { it.name == "Отпуск" }!!
        assertEquals(completed.targetAmount, completed.currentAmount)
        job.cancel()
    }

    @Test
    fun `incomplete goal has currentAmount less than targetAmount`() = runTest {
        val job = launch {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        val incomplete = viewModel.uiState.value.goals.find { it.name == "Ноутбук" }!!
        assert(incomplete.currentAmount < incomplete.targetAmount)
        job.cancel()
    }

    @Test
    fun `empty goals list is handled`() = runTest {
        every { goalDao.getAllGoals() } returns flowOf(emptyList())
        viewModel = GoalsViewModel(goalDao)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.goals.size)
    }
}