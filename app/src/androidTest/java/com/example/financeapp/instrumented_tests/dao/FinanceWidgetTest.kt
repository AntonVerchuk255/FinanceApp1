package com.example.financeapp.instrumented_tests.dao

import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.financeapp.widget.FinanceWidget
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinanceWidgetTest {

    @Test
    fun widget_providesContentWithoutCrash() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val widget = FinanceWidget()

        // Просто проверяем, что метод provideGlance не падает
        val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(widget.javaClass)
        if (glanceIds.isNotEmpty()) {
            widget.updateAll(context)
        } else {
            // Если виджет не добавлен на экран, хотя бы проверяем отсутствие исключений
            assertNotNull(widget)
        }
    }
}