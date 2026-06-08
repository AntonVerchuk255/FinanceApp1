package com.example.financeapp.instrumented_tests.dao

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.financeapp.data.AppDatabase
import com.example.financeapp.data.Reminder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: com.example.financeapp.data.ReminderDao

    @Before
    fun createDb() {
        db = androidx.room.Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.reminderDao()
    }

    @After
    fun closeDb() { db.close() }

    @Test
    fun insertAndGetReminder() = runTest {
        dao.insert(Reminder(title = "Аренда", amount = 10000L, categoryId = 1L, dayOfMonth = 5))
        val reminders = dao.getAllReminders().first()
        assertEquals(1, reminders.size)
        assertEquals("Аренда", reminders[0].title)
    }

    @Test
    fun getActiveRemindersByDay_returnsOnlyActive() = runTest {
        dao.insert(Reminder(title = "Активный", amount = 100L, categoryId = null, dayOfMonth = 10, isActive = true))
        dao.insert(Reminder(title = "Неактивный", amount = 200L, categoryId = null, dayOfMonth = 10, isActive = false))

        val active = dao.getActiveRemindersByDay(10)
        assertEquals(1, active.size)
        assertEquals("Активный", active[0].title)
    }
}