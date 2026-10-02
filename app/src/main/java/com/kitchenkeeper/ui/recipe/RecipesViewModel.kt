package com.kitchenkeeper.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitchenkeeper.data.KitchenRepository
import com.kitchenkeeper.data.PhotoStore
import com.kitchenkeeper.data.RecipeDao
import com.kitchenkeeper.data.RecipeWithIngredients
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipesViewModel(
    recipeDao: RecipeDao,
    private val repository: KitchenRepository,
    private val photoStore: PhotoStore,
) : ViewModel() {

    val recipes: StateFlow<List<RecipeWithIngredients>?> = recipeDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(recipe: RecipeWithIngredients) {
        viewModelScope.launch { repository.deleteRecipe(recipe) }
    }

    fun undoDelete(recipe: RecipeWithIngredients) {
        viewModelScope.launch { repository.restoreRecipe(recipe) }
    }

    fun finalizeDelete(recipe: RecipeWithIngredients) {
        photoStore.delete(recipe.recipe.photoPath)
    }
}
