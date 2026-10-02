package com.kitchenkeeper.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: FoodCategory,
    /** Absolute path in app-private storage, null if no photo. */
    val photoPath: String?,
    val expirationDate: LocalDate?,
    val createdAt: Instant,
    /** How much is on hand; [quantity] and [unit] are either both set or both null. */
    val quantity: Double? = null,
    val unit: MeasureUnit? = null,
)

val FoodItem.amount: Amount? get() = amountOf(quantity, unit)
