package com.example.financeapp.unit_tests.viewmodel

import com.example.financeapp.data.Converters
import com.example.financeapp.data.PeriodType
import com.example.financeapp.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `toInstant returns null when input is null`() {
        assertNull(converters.fromInstant(null))
    }

    @Test
    fun `toInstant converts epoch millis to Instant correctly`() {
        val millis = 1_700_000_000_000L
        val result = converters.fromInstant(millis)
        assertEquals(Instant.ofEpochMilli(millis), result)
    }

    @Test
    fun `fromInstant returns null when Instant is null`() {
        assertNull(converters.toInstant(null))
    }

    @Test
    fun `fromInstant converts Instant to epoch millis correctly`() {
        val instant = Instant.ofEpochMilli(1_700_000_000_000L)
        assertEquals(1_700_000_000_000L, converters.toInstant(instant))
    }

    @Test
    fun `Instant round-trip preserves value`() {
        val original = Instant.ofEpochMilli(1_234_567_890_123L)
        val millis = converters.toInstant(original)!!
        val restored = converters.fromInstant(millis)
        assertEquals(original, restored)
    }

    @Test
    fun `toInstant handles zero epoch millis`() {
        val result = converters.fromInstant(0L)
        assertEquals(Instant.EPOCH, result)
    }

    // ── TransactionType ↔ String ──────────────────────────────────────────────

    @Test
    fun `toTransactionType returns null when input is null`() {
        assertNull(converters.fromTransactionType(null))
    }

    @Test
    fun `toTransactionType converts INCOME string`() {
        assertEquals(TransactionType.INCOME, converters.fromTransactionType("INCOME"))
    }

    @Test
    fun `toTransactionType converts EXPENSE string`() {
        assertEquals(TransactionType.EXPENSE, converters.fromTransactionType("EXPENSE"))
    }

    @Test
    fun `toTransactionType converts TRANSFER string`() {
        assertEquals(TransactionType.TRANSFER, converters.fromTransactionType("TRANSFER"))
    }

    @Test
    fun `fromTransactionType returns null when type is null`() {
        assertNull(converters.toTransactionType(null))
    }

    @Test
    fun `fromTransactionType serialises INCOME to string`() {
        assertEquals("INCOME", converters.toTransactionType(TransactionType.INCOME))
    }

    @Test
    fun `fromTransactionType serialises EXPENSE to string`() {
        assertEquals("EXPENSE", converters.toTransactionType(TransactionType.EXPENSE))
    }

    @Test
    fun `fromTransactionType serialises TRANSFER to string`() {
        assertEquals("TRANSFER", converters.toTransactionType(TransactionType.TRANSFER))
    }

    @Test
    fun `TransactionType round-trip preserves all values`() {
        TransactionType.values().forEach { type ->
            val serialised = converters.toTransactionType(type)!!
            val restored = converters.fromTransactionType(serialised)
            assertEquals(type, restored)
        }
    }

    // ── PeriodType ↔ String ───────────────────────────────────────────────────

    @Test
    fun `toPeriodType returns null when input is null`() {
        assertNull(converters.fromPeriodType(null))
    }

    @Test
    fun `toPeriodType converts MONTHLY string`() {
        assertEquals(PeriodType.MONTHLY, converters.fromPeriodType("MONTHLY"))
    }

    @Test
    fun `toPeriodType converts WEEKLY string`() {
        assertEquals(PeriodType.WEEKLY, converters.fromPeriodType("WEEKLY"))
    }

    @Test
    fun `fromPeriodType returns null when type is null`() {
        assertNull(converters.toPeriodType(null))
    }

    @Test
    fun `fromPeriodType serialises MONTHLY to string`() {
        assertEquals("MONTHLY", converters.toPeriodType(PeriodType.MONTHLY))
    }

    @Test
    fun `fromPeriodType serialises WEEKLY to string`() {
        assertEquals("WEEKLY", converters.toPeriodType(PeriodType.WEEKLY))
    }

    @Test
    fun `PeriodType round-trip preserves all values`() {
        PeriodType.values().forEach { type ->
            val serialised = converters.toPeriodType(type)!!
            val restored = converters.fromPeriodType(serialised)
            assertEquals(type, restored)
        }
    }
}