package com.kitchenkeeper.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromCategory(value: FoodCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): FoodCategory = FoodCategory.valueOf(value)

    @TypeConverter
    fun fromMeasureUnit(value: MeasureUnit?): String? = value?.name

    @TypeConverter
    fun toMeasureUnit(value: String?): MeasureUnit? = value?.let(MeasureUnit::valueOf)

    @TypeConverter
    fun fromUsageUnit(value: UsageUnit): String = value.name

    @TypeConverter
    fun toUsageUnit(value: String): UsageUnit = UsageUnit.valueOf(value)

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): Long? = value?.toEpochDay()

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long): Instant = Instant.ofEpochMilli(value)
}
