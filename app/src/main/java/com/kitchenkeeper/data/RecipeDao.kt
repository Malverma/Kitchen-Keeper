package com.kitchenkeeper.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: Long): RecipeWithIngredients?

    @Insert
    suspend fun insert(recipe: Recipe): Long

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)

    @Insert
    suspend fun insertIngredients(ingredients: List<RecipeIngredient>)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredients(recipeId: Long)
}
