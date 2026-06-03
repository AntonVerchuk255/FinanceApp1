package com.example.financeapp.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ReportScreen(viewModel: ReportViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let { viewModel.generateReport(context, it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("PDF-отчёт", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Выберите период",
                        fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

                    // Выбор месяца
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            viewModel.setMonth(uiState.selectedMonth.minusMonths(1))
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Предыдущий")
                        }

                        val monthName = uiState.selectedMonth.month.getDisplayName(
                            TextStyle.FULL_STANDALONE, Locale("ru")
                        ).replaceFirstChar { it.uppercase() }

                        Text(
                            text = "$monthName ${uiState.selectedMonth.year}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        IconButton(
                            onClick = {
                                if (uiState.selectedMonth < YearMonth.now()) {
                                    viewModel.setMonth(uiState.selectedMonth.plusMonths(1))
                                }
                            },
                            enabled = uiState.selectedMonth < YearMonth.now()
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Следующий")
                        }
                    }

                    Text(
                        "Отчёт будет включать: сводку доходов и расходов, " +
                                "детализацию по категориям и полный список операций.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Button(
                            onClick = {
                                val monthName = uiState.selectedMonth.month.getDisplayName(
                                    TextStyle.FULL_STANDALONE, Locale("ru")
                                )
                                exportLauncher.launch(
                                    "report_${monthName}_${uiState.selectedMonth.year}.pdf"
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Сформировать PDF")
                        }
                    }
                }
            }
        }
    }
}