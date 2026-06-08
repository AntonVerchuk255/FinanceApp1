package com.example.financeapp.unit_tests.viewmodel

import com.example.financeapp.data.Category
import com.example.financeapp.data.Reminder
import com.example.financeapp.data.ReminderDao
import com.example.financeapp.data.CategoryDao
import com.example.financeapp.data.TransactionType
import com.example.financeapp.ui.reminders.RemindersViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemindersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var reminderDao: ReminderDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var viewModel: RemindersViewModel

    @Before
    fun setup() {
        reminderDao = mockk(relaxed = true)
        categoryDao = mockk(relaxed = true)
        every { reminderDao.getAllReminders() } returns flowOf(emptyList())
        every { categoryDao.getAllCategories() } returns flowOf(emptyList())
        viewModel = RemindersViewModel(reminderDao, categoryDao)
    }

    @Test
    fun `addReminder calls dao insert with correct data`() = runTest {
        viewModel.addReminder("Аренда", 15000L, 1L, 5)
        advanceUntilIdle()
        coVerify { reminderDao.insert(match {
            it.title == "Аренда" && it.amount == 15000L && it.categoryId == 1L && it.dayOfMonth == 5
        }) }
    }

    @Test
    fun `toggleReminder flips isActive flag`() = runTest {
        val reminder = Reminder(id = 1, title = "Тест", amount = 1000L, categoryId = null, dayOfMonth = 1, isActive = true)
        viewModel.toggleReminder(reminder)
        advanceUntilIdle()
        coVerify { reminderDao.update(match { !it.isActive }) }
    }

    @Test
    fun `deleteReminder calls dao delete`() = runTest {
        val reminder = Reminder(id = 1, title = "Тест", amount = 1000L, categoryId = null, dayOfMonth = 1)
        viewModel.deleteReminder(reminder)
        advanceUntilIdle()
        coVerify { reminderDao.delete(reminder) }
    }
}