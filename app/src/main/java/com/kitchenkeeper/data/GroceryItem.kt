package com.kitchenkeeper.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "grocery_items")
data class GroceryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: FoodCategory,
    /** Absolute path in app-private storage, null if no photo. Owned by this row. */
    val photoPath: String?,
    val quantity: Double?,
    val unit: MeasureUnit?,
    val createdAt: Instant,
)

val GroceryItem.amount: Amount? get() = amountOf(quantity, unit)
