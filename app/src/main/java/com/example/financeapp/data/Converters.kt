package com.example.financeapp.data

import androidx.room.TypeConverter
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun toInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun fromTransactionType(value: String?): TransactionType? =
        value?.let { TransactionType.valueOf(it) }

    @TypeConverter
    fun toTransactionType(type: TransactionType?): String? = type?.name

    @TypeConverter
    fun fromPeriodType(value: String?): PeriodType? =
        value?.let { PeriodType.valueOf(it) }

    @TypeConverter
    fun toPeriodType(type: PeriodType?): String? = type?.name
}