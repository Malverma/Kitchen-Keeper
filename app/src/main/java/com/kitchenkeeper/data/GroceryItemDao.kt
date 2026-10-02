package com.kitchenkeeper.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryItemDao {
    @Query("SELECT * FROM grocery_items ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE id = :id")
    suspend fun getById(id: Long): GroceryItem?

    @Query("SELECT * FROM grocery_items WHERE name = :name COLLATE NOCASE ORDER BY id")
    suspend fun findByName(name: String): List<GroceryItem>

    @Insert
    suspend fun insert(item: GroceryItem): Long

    @Update
    suspend fun update(item: GroceryItem)

    @Delete
    suspend fun delete(item: GroceryItem)
}
