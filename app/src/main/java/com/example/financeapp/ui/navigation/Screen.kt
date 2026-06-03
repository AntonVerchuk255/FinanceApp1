package com.example.financeapp.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object Analytics : Screen("analytics")
    object Budgets : Screen("budgets")
    object Goals : Screen("goals")
    object Settings : Screen("settings")
    object Comparison : Screen("comparison")
    object Reminders : Screen("reminders")
    object Report : Screen("report")
}