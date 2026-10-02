package com.kitchenkeeper.data

import androidx.room.withTransaction
import java.time.Instant

/** Operations that touch more than one table. */
class KitchenRepository(
    private val db: KitchenKeeperDatabase,
    private val photoStore: PhotoStore,
) {
    private val foodDao = db.foodItemDao()
    private val recipeDao = db.recipeDao()
    private val groceryDao = db.groceryItemDao()

    /** Inserts or updates [recipe] and replaces its ingredient list. Returns the recipe id. */
    suspend fun saveRecipe(recipe: Recipe, ingredients: List<RecipeIngredient>): Long = db.withTransaction {
        val id = if (recipe.id == 0L) {
            recipeDao.insert(recipe)
        } else {
            recipeDao.update(recipe)
            recipeDao.deleteIngredients(recipe.id)
            recipe.id
        }
        recipeDao.insertIngredients(ingredients.map { it.copy(id = 0, recipeId = id) })
        id
    }

    /** Deletes the recipe but leaves its photo so it can be restored. */
    suspend fun deleteRecipe(recipe: RecipeWithIngredients) = db.withTransaction {
        recipeDao.deleteIngredients(recipe.recipe.id)
        recipeDao.delete(recipe.recipe)
    }

    suspend fun restoreRecipe(recipe: RecipeWithIngredients) = db.withTransaction {
        recipeDao.insert(recipe.recipe)
        recipeDao.insertIngredients(recipe.ingredients)
    }

    /**
     * Uses each ingredient of the recipe: subtracts it from the pantry (removing the item once it
     * runs out) and adds the amount used to the grocery list. Returns how many ingredients were used.
     */
    suspend fun cook(recipeId: Long): Int {
        val usedUpPhotos = mutableListOf<String>()
        val count = db.withTransaction {
            val recipe = recipeDao.getById(recipeId) ?: return@withTransaction 0
            for (ingredient in recipe.ingredients) {
                val used = ingredient.usedAmount ?: continue
                val pantryItem = ingredient.foodItemId?.let { foodDao.getById(it) }
                    ?: foodDao.findByName(ingredient.name)
                if (pantryItem != null) {
                    if (subtractFromPantry(pantryItem, used)) pantryItem.photoPath?.let(usedUpPhotos::add)
                }
                addToGroceryList(
                    name = pantryItem?.name ?: ingredient.name,
                    category = pantryItem?.category ?: ingredient.category,
                    amount = used,
                )
            }
            recipe.ingredients.size
        }
        usedUpPhotos.forEach(photoStore::delete)
        return count
    }

    /** Returns true if the item was used up and removed. */
    private suspend fun subtractFromPantry(item: FoodItem, used: Amount): Boolean {
        val onHand = item.amount
        val usedInPantryUnits = onHand?.let { used.convertTo(it.unit) }
        // No amount on record, or units that can't be compared: treat the item as used up.
        if (onHand == null || usedInPantryUnits == null || onHand.quantity - usedInPantryUnits <= EPSILON) {
            foodDao.delete(item)
            return true
        }
        foodDao.update(item.copy(quantity = roundQuantity(onHand.quantity - usedInPantryUnits)))
        return false
    }

    /** Adds to an existing grocery entry with a compatible unit, or creates a new one. */
    suspend fun addToGroceryList(name: String, category: FoodCategory, amount: Amount?) {
        val existing = groceryDao.findByName(name)
        if (amount != null) {
            for (entry in existing) {
                val entryAmount = entry.amount ?: continue
                val added = amount.convertTo(entryAmount.unit) ?: continue
                groceryDao.update(entry.copy(quantity = roundQuantity(entryAmount.quantity + added)))
                return
            }
        } else if (existing.any { it.amount == null }) {
            return
        }
        groceryDao.insert(
            GroceryItem(
                name = name,
                category = category,
                quantity = amount?.quantity,
                unit = amount?.unit,
                createdAt = Instant.now(),
            ),
        )
    }

    /** Marks a grocery item as bought: moves it into the pantry and off the list. */
    suspend fun restock(item: GroceryItem) = db.withTransaction {
        val bought = item.amount
        val pantryItem = foodDao.findByName(item.name)
        val onHand = pantryItem?.amount
        val boughtInPantryUnits = if (bought != null && onHand != null) bought.convertTo(onHand.unit) else null
        if (pantryItem != null && onHand != null && boughtInPantryUnits != null) {
            foodDao.update(pantryItem.copy(quantity = roundQuantity(onHand.quantity + boughtInPantryUnits)))
        } else {
            foodDao.insert(
                FoodItem(
                    name = item.name,
                    category = item.category,
                    photoPath = null,
                    expirationDate = null,
                    createdAt = Instant.now(),
                    quantity = bought?.quantity,
                    unit = bought?.unit,
                ),
            )
        }
        groceryDao.delete(item)
    }

    private companion object {
        const val EPSILON = 0.0001
    }
}
