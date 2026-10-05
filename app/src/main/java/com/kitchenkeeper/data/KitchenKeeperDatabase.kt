package com.kitchenkeeper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [FoodItem::class, Recipe::class, RecipeIngredient::class, GroceryItem::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class KitchenKeeperDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun recipeDao(): RecipeDao
    abstract fun groceryItemDao(): GroceryItemDao

    companion object {
        fun create(context: Context): KitchenKeeperDatabase =
            Room.databaseBuilder(context, KitchenKeeperDatabase::class.java, "kitchen_keeper.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }
}

/** Adds pantry amounts, recipes, and the grocery list. */
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE food_items ADD COLUMN quantity REAL")
        db.execSQL("ALTER TABLE food_items ADD COLUMN unit TEXT")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `recipes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, `photoPath` TEXT, `instructions` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `recipe_ingredients` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`recipeId` INTEGER NOT NULL, `foodItemId` INTEGER, `name` TEXT NOT NULL, `category` TEXT NOT NULL, " +
                "`amount` REAL, `unit` TEXT NOT NULL, FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_recipe_ingredients_recipeId` ON `recipe_ingredients` (`recipeId`)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `grocery_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, `category` TEXT NOT NULL, `quantity` REAL, `unit` TEXT, " +
                "`createdAt` INTEGER NOT NULL)",
        )
    }
}

/** Lets grocery items keep the photo of the pantry item they came from. */
private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE grocery_items ADD COLUMN photoPath TEXT")
    }
}
