package com.kitchenkeeper.ui.recipe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitchenkeeper.data.FoodCategory
import com.kitchenkeeper.data.FoodItem
import com.kitchenkeeper.data.FoodItemDao
import com.kitchenkeeper.data.KitchenRepository
import com.kitchenkeeper.data.PhotoStore
import com.kitchenkeeper.data.Recipe
import com.kitchenkeeper.data.RecipeDao
import com.kitchenkeeper.data.RecipeIngredient
import com.kitchenkeeper.data.RecipeWithIngredients
import com.kitchenkeeper.data.UsageUnit
import com.kitchenkeeper.data.formatQuantity
import com.kitchenkeeper.ui.components.PhotoSession
import com.kitchenkeeper.ui.components.parseQuantity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

/** An ingredient row being edited on the recipe form. */
data class IngredientDraft(
    val foodItemId: Long?,
    val name: String,
    val category: FoodCategory,
    val quantityText: String,
    val unit: UsageUnit,
) {
    val quantity: Double? get() = if (unit.needsQuantity) parseQuantity(quantityText) else null
    val isValid: Boolean get() = !unit.needsQuantity || quantity != null

    fun toIngredient() = RecipeIngredient(
        recipeId = 0,
        foodItemId = foodItemId,
        name = name,
        category = category,
        amount = quantity,
        unit = unit,
    )

    fun usageLabel(): String = toIngredient().usageLabel()

    /** The pantry item this ingredient draws from: by id, or by name once the original was used up. */
    fun findIn(pantry: List<FoodItem>): FoodItem? =
        pantry.firstOrNull { it.id == foodItemId } ?: pantry.firstOrNull { it.name.equals(name, ignoreCase = true) }
}

/** Backs both the Add and Edit Recipe screens. */
class RecipeViewModel(
    savedStateHandle: SavedStateHandle,
    private val recipeDao: RecipeDao,
    foodDao: FoodItemDao,
    private val repository: KitchenRepository,
    photoStore: PhotoStore,
) : ViewModel() {

    private var recipeId: Long = savedStateHandle[RECIPE_ID_ARG] ?: NEW_RECIPE_ID
    val isEditing: Boolean = recipeId != NEW_RECIPE_ID

    private var original: RecipeWithIngredients? = null
    val photo = PhotoSession(photoStore)

    val pantry: StateFlow<List<FoodItem>> = foodDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var loaded by mutableStateOf(!isEditing)
        private set
    var name by mutableStateOf("")
    var instructions by mutableStateOf("")
    val ingredients = mutableStateListOf<IngredientDraft>()

    private var busy by mutableStateOf(false)

    val canSave: Boolean
        get() = loaded && !busy && name.isNotBlank() && ingredients.all { it.isValid }

    val canCook: Boolean
        get() = canSave && ingredients.isNotEmpty()

    init {
        if (isEditing) {
            viewModelScope.launch {
                recipeDao.getById(recipeId)?.let { recipe ->
                    original = recipe
                    name = recipe.recipe.name
                    instructions = recipe.recipe.instructions
                    photo.load(recipe.recipe.photoPath)
                    ingredients += recipe.ingredients.map {
                        IngredientDraft(
                            foodItemId = it.foodItemId,
                            name = it.name,
                            category = it.category,
                            quantityText = it.amount?.let(::formatQuantity).orEmpty(),
                            unit = it.unit,
                        )
                    }
                }
                loaded = true
            }
        }
    }

    fun addIngredient(item: FoodItem) {
        ingredients += IngredientDraft(
            foodItemId = item.id,
            name = item.name,
            category = item.category,
            quantityText = "",
            unit = UsageUnit.defaultFor(item.unit),
        )
    }

    fun updateIngredient(index: Int, draft: IngredientDraft) {
        ingredients[index] = draft
    }

    fun removeIngredient(index: Int) {
        ingredients.removeAt(index)
    }

    fun save(onSaved: () -> Unit) {
        if (!canSave) return
        viewModelScope.launch {
            persist()
            onSaved()
        }
    }

    /** Saves any edits, then cooks the meal. Reports how many items went onto the grocery list. */
    fun cook(onCooked: (recipeName: String, itemCount: Int) -> Unit) {
        if (!canCook) return
        viewModelScope.launch {
            val id = persist()
            val count = repository.cook(id)
            onCooked(name.trim(), count)
        }
    }

    private suspend fun persist(): Long {
        busy = true
        try {
            val recipe = Recipe(
                id = original?.recipe?.id ?: 0,
                name = name.trim(),
                photoPath = photo.photoPath,
                instructions = instructions.trim(),
                createdAt = original?.recipe?.createdAt ?: Instant.now(),
            )
            val ingredients = ingredients.map { it.toIngredient() }
            val id = repository.saveRecipe(recipe, ingredients)
            photo.commit()
            original = RecipeWithIngredients(recipe.copy(id = id), ingredients)
            recipeId = id
            return id
        } finally {
            busy = false
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val existing = original ?: return
        viewModelScope.launch {
            repository.deleteRecipe(existing)
            photo.deleteAll()
            onDeleted()
        }
    }

    override fun onCleared() {
        photo.discard()
    }

    companion object {
        const val RECIPE_ID_ARG = "id"
        const val NEW_RECIPE_ID = -1L
    }
}
