package com.example.financeapp.unit_tests.viewmodel

import android.content.Context
import android.net.Uri
import com.example.financeapp.data.AppDatabase
import com.example.financeapp.data.BackupManager
import com.example.financeapp.ui.settings.SettingsViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var db: AppDatabase
    private lateinit var backupManager: BackupManager
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        db = mockk(relaxed = true)
        backupManager = mockk(relaxed = true)
        viewModel = SettingsViewModel(db, backupManager)  // инжектим мок
    }

    @Test
    fun `exportBackup shows success message on success`() = runTest {
        val uri = mockk<Uri>()
        val context = mockk<Context>(relaxed = true)
        coEvery { backupManager.exportBackup(context, uri) } returns Result.success(Unit)

        viewModel.exportBackup(context, uri)
        advanceUntilIdle()

        assertEquals("Резервная копия сохранена", viewModel.uiState.value.message)
    }

    @Test
    fun `importBackup shows error message on failure`() = runTest {
        val uri = mockk<Uri>()
        val context = mockk<Context>(relaxed = true)
        coEvery { backupManager.importBackup(context, uri) } returns Result.failure(Exception("Ошибка"))

        viewModel.importBackup(context, uri)
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.message?.contains("Ошибка") ?: false)
    }
}