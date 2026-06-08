package com.example.financeapp.unit_tests.viewmodel

import com.example.financeapp.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ScreenTest {

    @Test
    fun `Dashboard route is dashboard`() {
        assertEquals("dashboard", Screen.Dashboard.route)
    }

    @Test
    fun `Transactions route is transactions`() {
        assertEquals("transactions", Screen.Transactions.route)
    }

    @Test
    fun `Analytics route is analytics`() {
        assertEquals("analytics", Screen.Analytics.route)
    }

    @Test
    fun `Budgets route is budgets`() {
        assertEquals("budgets", Screen.Budgets.route)
    }

    @Test
    fun `Goals route is goals`() {
        assertEquals("goals", Screen.Goals.route)
    }

    @Test
    fun `Settings route is settings`() {
        assertEquals("settings", Screen.Settings.route)
    }

    @Test
    fun `Comparison route is comparison`() {
        assertEquals("comparison", Screen.Comparison.route)
    }

    @Test
    fun `Reminders route is reminders`() {
        assertEquals("reminders", Screen.Reminders.route)
    }

    @Test
    fun `Report route is report`() {
        assertEquals("report", Screen.Report.route)
    }

    @Test
    fun `all routes are unique`() {
        val screens = listOf(
            Screen.Dashboard, Screen.Transactions, Screen.Analytics,
            Screen.Budgets, Screen.Goals, Screen.Settings,
            Screen.Comparison, Screen.Reminders, Screen.Report
        )
        val routes = screens.map { it.route }
        assertEquals("Все маршруты должны быть уникальными", routes.size, routes.toSet().size)
    }

    @Test
    fun `routes do not contain spaces`() {
        val screens = listOf(
            Screen.Dashboard, Screen.Transactions, Screen.Analytics,
            Screen.Budgets, Screen.Goals, Screen.Settings,
            Screen.Comparison, Screen.Reminders, Screen.Report
        )
        screens.forEach { screen ->
            assert(!screen.route.contains(" ")) {
                "Маршрут '${screen.route}' содержит пробел"
            }
        }
    }

    @Test
    fun `Dashboard and Transactions are different screens`() {
        assertNotEquals(Screen.Dashboard.route, Screen.Transactions.route)
    }
}