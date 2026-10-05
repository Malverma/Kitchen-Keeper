package com.kitchenkeeper.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kitchenkeeper.KitchenKeeperApp
import com.kitchenkeeper.ui.item.ItemViewModel
import com.kitchenkeeper.ui.pantry.PantryViewModel
import com.kitchenkeeper.ui.recipe.RecipeViewModel
import com.kitchenkeeper.ui.recipe.RecipesViewModel
import com.kitchenkeeper.ui.shopping.GroceryViewModel
import com.kitchenkeeper.ui.shopping.ShoppingViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = kitchenKeeperApp()
            PantryViewModel(app.database.foodItemDao(), app.photoStore)
        }
        initializer {
            val app = kitchenKeeperApp()
            ItemViewModel(createSavedStateHandle(), app.database.foodItemDao(), app.photoStore)
        }
        initializer {
            val app = kitchenKeeperApp()
            RecipesViewModel(app.database.recipeDao(), app.repository, app.photoStore)
        }
        initializer {
            val app = kitchenKeeperApp()
            RecipeViewModel(
                createSavedStateHandle(),
                app.database.recipeDao(),
                app.database.foodItemDao(),
                app.repository,
                app.photoStore,
            )
        }
        initializer {
            val app = kitchenKeeperApp()
            ShoppingViewModel(app.database.groceryItemDao(), app.repository, app.photoStore)
        }
        initializer {
            val app = kitchenKeeperApp()
            GroceryViewModel(
                createSavedStateHandle(),
                app.database.groceryItemDao(),
                app.repository,
                app.photoStore,
            )
        }
    }
}

private fun CreationExtras.kitchenKeeperApp(): KitchenKeeperApp =
    this[APPLICATION_KEY] as KitchenKeeperApp
