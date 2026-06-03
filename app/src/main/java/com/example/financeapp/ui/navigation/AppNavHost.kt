package com.example.financeapp.ui.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.financeapp.data.AppDatabase
import com.example.financeapp.ui.FinanceChartScreen
import com.example.financeapp.ui.dashboard.DashboardScreen
import com.example.financeapp.ui.transactions.TransactionsScreen
import com.example.financeapp.ui.budgets.BudgetsScreen
import com.example.financeapp.ui.goals.GoalsScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financeapp.ui.AnalyticsViewModel
import com.example.financeapp.ui.AnalyticsViewModelFactory
import com.example.financeapp.ui.analytics.ComparisonScreen
import com.example.financeapp.ui.analytics.ComparisonViewModel
import com.example.financeapp.ui.analytics.ComparisonViewModelFactory
import com.example.financeapp.ui.budgets.BudgetsViewModel
import com.example.financeapp.ui.budgets.BudgetsViewModelFactory
import com.example.financeapp.ui.dashboard.DashboardViewModel
import com.example.financeapp.ui.dashboard.DashboardViewModelFactory
import com.example.financeapp.ui.dashboard.DashboardScreen
import com.example.financeapp.ui.goals.GoalsViewModel
import com.example.financeapp.ui.goals.GoalsViewModelFactory
import com.example.financeapp.ui.reminders.RemindersScreen
import com.example.financeapp.ui.reminders.RemindersViewModel
import com.example.financeapp.ui.reminders.RemindersViewModelFactory
import com.example.financeapp.ui.settings.ReportScreen
import com.example.financeapp.ui.settings.ReportViewModel
import com.example.financeapp.ui.settings.ReportViewModelFactory
import com.example.financeapp.ui.settings.SettingsScreen
import com.example.financeapp.ui.settings.SettingsViewModel
import com.example.financeapp.ui.settings.SettingsViewModelFactory
import com.example.financeapp.ui.transactions.TransactionsViewModel
import com.example.financeapp.ui.transactions.TransactionsViewModelFactory

@Composable
fun AppNavHost(
    navController: NavHostController,
    db: AppDatabase,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        //composable(Screen.Dashboard.route) {
        //    DashboardScreen()
        //}
        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(db.transactionDao())
            )
            DashboardScreen(viewModel = viewModel)
        }
        composable(Screen.Transactions.route) {
            val viewModel: TransactionsViewModel = viewModel(
                factory = TransactionsViewModelFactory(
                    db.transactionDao(),
                    db.categoryDao(),
                    context
                )
            )
            TransactionsScreen(viewModel = viewModel)
        }
        composable(Screen.Analytics.route) {
            val viewModel: AnalyticsViewModel = viewModel(
                factory = AnalyticsViewModelFactory(db.transactionDao())
            )
            FinanceChartScreen(
                viewModel = viewModel,
                onNavigateToComparison = { navController.navigate(Screen.Comparison.route) }
            )
        }
        composable(Screen.Budgets.route) {
            val viewModel: BudgetsViewModel = viewModel(
                factory = BudgetsViewModelFactory(
                    db.budgetDao(),
                    db.transactionDao(),
                    db.categoryDao()
                )
            )
            BudgetsScreen(viewModel = viewModel)
        }
        composable(Screen.Goals.route) {
            val viewModel: GoalsViewModel = viewModel(
                factory = GoalsViewModelFactory(db.goalDao())
            )
            GoalsScreen(viewModel = viewModel)
        }
        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(db)
            )
            SettingsScreen(
                viewModel = viewModel,
                onNavigateToReport = { navController.navigate(Screen.Report.route) }
            )
        }

        composable(Screen.Report.route) {
            val viewModel: ReportViewModel = viewModel(
                factory = ReportViewModelFactory(db)
            )
            ReportScreen(viewModel = viewModel)
        }
        composable(Screen.Comparison.route) {
            val viewModel: ComparisonViewModel = viewModel(
                factory = ComparisonViewModelFactory(db.transactionDao())
            )
            ComparisonScreen(viewModel = viewModel)
        }
        composable(Screen.Reminders.route) {
            val viewModel: RemindersViewModel = viewModel(
                factory = RemindersViewModelFactory(
                    db.reminderDao(),
                    db.categoryDao()
                )
            )
            RemindersScreen(viewModel = viewModel)
        }
    }
}