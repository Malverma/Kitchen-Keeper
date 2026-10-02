package com.kitchenkeeper.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.Instant

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val photoPath: String?,
    val instructions: String,
    val createdAt: Instant,
)

/**
 * A pantry item used by a recipe. Name and category are copied from the pantry item so the
 * recipe still makes sense after that item is used up and removed from the pantry.
 */
@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("recipeId")],
)
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val foodItemId: Long?,
    val name: String,
    val category: FoodCategory,
    /** Null for container units, which carry their own quantity. */
    val amount: Double?,
    val unit: UsageUnit,
) {
    val usedAmount: Amount? get() = unit.toAmount(amount)

    fun usageLabel(): String =
        if (unit.needsQuantity) usedAmount?.formatted().orEmpty() else unit.label
}

data class RecipeWithIngredients(
    @Embedded val recipe: Recipe,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val ingredients: List<RecipeIngredient>,
)
