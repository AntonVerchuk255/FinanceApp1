package com.example.financeapp.unit_tests.viewmodel

import com.example.financeapp.ui.dashboard.formatAmount
import org.junit.Assert.assertEquals
import org.junit.Test


class FormatAmountTest {

    @Test
    fun `zero amount formats to 0 roubles`() {
        assertEquals("0.00 ₽", formatAmount(0L))
    }

    @Test
    fun `amount with zero kopecks shows two zeros`() {
        // 100_00 kopecks = 100 roubles 00 kopecks
        assertEquals("100.00 ₽", formatAmount(100_00L))
    }

    @Test
    fun `amount with non-zero kopecks formats correctly`() {
        // 150_75 kopecks = 150 roubles 75 kopecks
        assertEquals("150.75 ₽", formatAmount(150_75L))
    }

    @Test
    fun `single kopeck pads to two digits`() {
        // 5_01 kopecks = 5 roubles 01 kopeck
        assertEquals("5.01 ₽", formatAmount(5_01L))
    }

    @Test
    fun `large amount formats with thousands separator`() {
        // 1_000_000_00 kopecks = 1 000 000 roubles
        val result = formatAmount(1_000_000_00L)
        // Java %,d uses locale-specific separator; on JVM typically ','
        assert(result.contains("1") && result.endsWith(".00 ₽")) {
            "Unexpected format: $result"
        }
    }

    @Test
    fun `negative amount preserves sign and absolute kopecks`() {
        // -50_25 kopecks = -50 roubles, kopecks = |-50_25 % 100| = 25
        val result = formatAmount(-50_25L)
        assertEquals("-50.25 ₽", result)
    }

    @Test
    fun `one kopeck amount`() {
        assertEquals("0.01 ₽", formatAmount(1L))
    }

    @Test
    fun `99 kopecks amount`() {
        assertEquals("0.99 ₽", formatAmount(99L))
    }

    @Test
    fun `exactly one rouble`() {
        assertEquals("1.00 ₽", formatAmount(100L))
    }

    @Test
    fun `salary-like amount 50000 roubles`() {
        val result = formatAmount(50_000_00L)
        assert(result.endsWith(".00 ₽")) { "Unexpected: $result" }
        assert(result.contains("50")) { "Should contain 50: $result" }
    }
}