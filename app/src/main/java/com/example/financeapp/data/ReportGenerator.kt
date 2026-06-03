package com.example.financeapp.data

import android.content.Context
import android.net.Uri
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class ReportGenerator(private val db: AppDatabase) {

    private fun loadFont(context: Context, name: String): PdfFont {
        val bytes = context.assets.open("fonts/$name").readBytes()
        return PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H)
    }

    suspend fun generateMonthlyReport(
        context: Context,
        uri: Uri,
        yearMonth: YearMonth
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val regularFont = loadFont(context, "Roboto-Regular.ttf")
            val boldFont = loadFont(context, "Roboto-Bold.ttf")

            val zone = ZoneId.systemDefault()
            val start = yearMonth.atDay(1).atStartOfDay(zone).toInstant()
            val end = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant()

            val transactions = db.transactionDao().getTransactionsByPeriodOnce(start, end)
            val categories = db.categoryDao().getAllCategoriesOnce()
            val expenseCats = db.transactionDao()
                .getCategoryTotalsOnce(TransactionType.EXPENSE, start, end)
            val incomeCats = db.transactionDao()
                .getCategoryTotalsOnce(TransactionType.INCOME, start, end)
            val totalIncome = transactions.filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = PdfWriter(outputStream)
                val pdf = PdfDocument(writer)
                val document = Document(pdf)

                val monthName = yearMonth.month.getDisplayName(
                    TextStyle.FULL_STANDALONE, Locale("ru")
                ).replaceFirstChar { it.uppercase() }

                // Заголовок
                document.add(
                    Paragraph("Финансовый отчёт")
                        .setFont(boldFont)
                        .setFontSize(24f)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(4f)
                )
                document.add(
                    Paragraph("$monthName ${yearMonth.year}")
                        .setFont(regularFont)
                        .setFontSize(16f)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.GRAY)
                        .setMarginBottom(20f)
                )

                // Сводка
                document.add(sectionTitle("Сводка", boldFont))
                document.add(summaryTable(totalIncome, totalExpense, regularFont, boldFont))

                // Расходы по категориям
                if (expenseCats.isNotEmpty()) {
                    document.add(sectionTitle("Расходы по категориям", boldFont))
                    document.add(categoryTable(expenseCats, totalExpense, regularFont, boldFont))
                }

                // Доходы по категориям
                if (incomeCats.isNotEmpty()) {
                    document.add(sectionTitle("Доходы по категориям", boldFont))
                    document.add(categoryTable(incomeCats, totalIncome, regularFont, boldFont))
                }

                // Список транзакций
                if (transactions.isNotEmpty()) {
                    document.add(sectionTitle("Все операции", boldFont))
                    document.add(transactionsTable(transactions, categories, regularFont, boldFont))
                }

                // Подвал
                document.add(
                    Paragraph("Сформировано: ${
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                            .withZone(zone)
                            .format(Instant.now())
                    }")
                        .setFont(regularFont)
                        .setFontSize(9f)
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setMarginTop(20f)
                )

                document.close()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sectionTitle(text: String, boldFont: PdfFont): Paragraph =
        Paragraph(text)
            .setFont(boldFont)
            .setFontSize(14f)
            .setMarginTop(16f)
            .setMarginBottom(8f)
            .setBorderBottom(SolidBorder(DeviceRgb(200, 200, 200), 1f))
            .setPaddingBottom(4f)

    private fun headerCell(text: String, boldFont: PdfFont): Cell =
        Cell().add(
            Paragraph(text)
                .setFont(boldFont)
                .setFontSize(10f)
        )
            .setBackgroundColor(DeviceRgb(240, 240, 240))
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(6f)

    private fun summaryTable(
        totalIncome: Long,
        totalExpense: Long,
        regularFont: PdfFont,
        boldFont: PdfFont
    ): Table {
        val balance = totalIncome - totalExpense
        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f, 1f)))
            .useAllAvailableWidth()
            .setMarginBottom(12f)

        listOf("Доходы", "Расходы", "Баланс").forEach { header ->
            table.addHeaderCell(headerCell(header, boldFont))
        }

        table.addCell(amountCell(totalIncome, DeviceRgb(0, 150, 50), boldFont))
        table.addCell(amountCell(totalExpense, DeviceRgb(200, 0, 0), boldFont))
        table.addCell(amountCell(
            balance,
            if (balance >= 0) DeviceRgb(0, 150, 50) else DeviceRgb(200, 0, 0),
            boldFont
        ))

        return table
    }

    private fun amountCell(amount: Long, color: DeviceRgb, boldFont: PdfFont): Cell =
        Cell().add(
            Paragraph(formatAmountPdf(amount))
                .setFont(boldFont)
                .setFontColor(color)
                .setFontSize(12f)
        )
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(8f)

    private fun categoryTable(
        categories: List<CategorySum>,
        total: Long,
        regularFont: PdfFont,
        boldFont: PdfFont
    ): Table {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f, 1f)))
            .useAllAvailableWidth()
            .setMarginBottom(12f)

        listOf("Категория", "Сумма", "Доля").forEach { header ->
            table.addHeaderCell(headerCell(header, boldFont))
        }

        categories.forEach { cat ->
            val percent = if (total > 0) cat.total.toFloat() / total * 100 else 0f
            table.addCell(
                Cell().add(Paragraph(cat.name).setFont(regularFont).setFontSize(10f))
                    .setPadding(6f)
            )
            table.addCell(
                Cell().add(Paragraph(formatAmountPdf(cat.total)).setFont(regularFont).setFontSize(10f))
                    .setTextAlignment(TextAlignment.RIGHT).setPadding(6f)
            )
            table.addCell(
                Cell().add(Paragraph("%.1f%%".format(percent)).setFont(regularFont).setFontSize(10f))
                    .setTextAlignment(TextAlignment.RIGHT).setPadding(6f)
            )
        }

        return table
    }

    private fun transactionsTable(
        transactions: List<Transaction>,
        categories: List<Category>,
        regularFont: PdfFont,
        boldFont: PdfFont
    ): Table {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            .withZone(ZoneId.systemDefault())

        val table = Table(UnitValue.createPercentArray(floatArrayOf(2f, 3f, 2f, 2f)))
            .useAllAvailableWidth()
            .setMarginBottom(12f)

        listOf("Дата", "Категория / Заметка", "Тип", "Сумма").forEach { header ->
            table.addHeaderCell(headerCell(header, boldFont))
        }

        transactions.sortedByDescending { it.date }.forEach { t ->
            val catName = categories.find { it.id == t.categoryId }?.name ?: "—"
            val typeLabel = when (t.type) {
                TransactionType.INCOME -> "Доход"
                TransactionType.EXPENSE -> "Расход"
                TransactionType.TRANSFER -> "Перевод"
            }
            val typeColor = when (t.type) {
                TransactionType.INCOME -> DeviceRgb(0, 150, 50)
                TransactionType.EXPENSE -> DeviceRgb(200, 0, 0)
                TransactionType.TRANSFER -> DeviceRgb(0, 100, 200)
            }

            table.addCell(
                Cell().add(Paragraph(formatter.format(t.date))
                    .setFont(regularFont).setFontSize(9f)).setPadding(5f)
            )
            table.addCell(
                Cell().add(
                    Paragraph("$catName${if (!t.note.isNullOrEmpty()) "\n${t.note}" else ""}")
                        .setFont(regularFont).setFontSize(9f)
                ).setPadding(5f)
            )
            table.addCell(
                Cell().add(Paragraph(typeLabel)
                    .setFont(regularFont)
                    .setFontColor(typeColor)
                    .setFontSize(9f)).setPadding(5f)
            )
            table.addCell(
                Cell().add(Paragraph(formatAmountPdf(t.amount))
                    .setFont(regularFont).setFontSize(9f))
                    .setTextAlignment(TextAlignment.RIGHT).setPadding(5f)
            )
        }

        return table
    }

    private fun formatAmountPdf(amount: Long): String {
        val rubles = amount / 100
        val kopecks = Math.abs(amount % 100)
        return "%,d.%02d р.".format(rubles, kopecks)
    }
}