package com.example.financeapp.instrumented_tests.dao

import com.example.financeapp.data.Converters
import com.example.financeapp.data.PeriodType
import com.example.financeapp.data.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setup() {
        converters = Converters()
    }

    @Test
    fun fromInstant_withNull_returnsNull() {
        assertNull(converters.fromInstant(null))
    }

    @Test
    fun fromInstant_withValidLong_returnsInstant() {
        val millis = System.currentTimeMillis()
        val instant = converters.fromInstant(millis)

        assertNotNull(instant)
        assertEquals(millis, instant?.toEpochMilli())
    }

    @Test
    fun toInstant_withNull_returnsNull() {
        assertNull(converters.toInstant(null))
    }

    @Test
    fun toInstant_withValidInstant_returnsMillis() {
        val instant = Instant.now()
        val millis = converters.toInstant(instant)

        assertNotNull(millis)
        assertEquals(instant.toEpochMilli(), millis)
    }

    @Test
    fun fromTransactionType_withNull_returnsNull() {
        assertNull(converters.fromTransactionType(null))
    }

    @Test
    fun fromTransactionType_withValidString_returnsTransactionType() {
        assertEquals(TransactionType.INCOME, converters.fromTransactionType("INCOME"))
        assertEquals(TransactionType.EXPENSE, converters.fromTransactionType("EXPENSE"))
        assertEquals(TransactionType.TRANSFER, converters.fromTransactionType("TRANSFER"))
    }

    @Test
    fun toTransactionType_withNull_returnsNull() {
        assertNull(converters.toTransactionType(null))
    }

    @Test
    fun toTransactionType_withValidType_returnsString() {
        assertEquals("INCOME", converters.toTransactionType(TransactionType.INCOME))
        assertEquals("EXPENSE", converters.toTransactionType(TransactionType.EXPENSE))
        assertEquals("TRANSFER", converters.toTransactionType(TransactionType.TRANSFER))
    }

    @Test
    fun fromPeriodType_withNull_returnsNull() {
        assertNull(converters.fromPeriodType(null))
    }

    @Test
    fun fromPeriodType_withValidString_returnsPeriodType() {
        assertEquals(PeriodType.MONTHLY, converters.fromPeriodType("MONTHLY"))
        assertEquals(PeriodType.WEEKLY, converters.fromPeriodType("WEEKLY"))
    }

    @Test
    fun toPeriodType_withNull_returnsNull() {
        assertNull(converters.toPeriodType(null))
    }

    @Test
    fun toPeriodType_withValidType_returnsString() {
        assertEquals("MONTHLY", converters.toPeriodType(PeriodType.MONTHLY))
        assertEquals("WEEKLY", converters.toPeriodType(PeriodType.WEEKLY))
    }

    @Test
    fun instantConversion_roundTrip_preservesValue() {
        val original = Instant.now()
        val millis = converters.toInstant(original)
        val restored = converters.fromInstant(millis)

        assertEquals(original, restored)
    }

    @Test
    fun transactionTypeConversion_roundTrip_preservesValue() {
        val original = TransactionType.EXPENSE
        val string = converters.toTransactionType(original)
        val restored = converters.fromTransactionType(string)

        assertEquals(original, restored)
    }

    @Test
    fun periodTypeConversion_roundTrip_preservesValue() {
        val original = PeriodType.WEEKLY
        val string = converters.toPeriodType(original)
        val restored = converters.fromPeriodType(string)

        assertEquals(original, restored)
    }
}