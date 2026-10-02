package com.kitchenkeeper.ui.shopping

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitchenkeeper.data.FoodCategory
import com.kitchenkeeper.data.GroceryItem
import com.kitchenkeeper.data.GroceryItemDao
import com.kitchenkeeper.data.KitchenRepository
import com.kitchenkeeper.data.MeasureUnit
import com.kitchenkeeper.data.amountOf
import com.kitchenkeeper.data.formatQuantity
import com.kitchenkeeper.ui.components.parseQuantity
import kotlinx.coroutines.launch

/** Backs both the Add and Edit Grocery Item screens. */
class GroceryViewModel(
    savedStateHandle: SavedStateHandle,
    private val dao: GroceryItemDao,
    private val repository: KitchenRepository,
) : ViewModel() {

    private val itemId: Long = savedStateHandle[GROCERY_ID_ARG] ?: NEW_GROCERY_ID
    val isEditing: Boolean = itemId != NEW_GROCERY_ID

    private var original: GroceryItem? = null

    var loaded by mutableStateOf(!isEditing)
        private set
    var name by mutableStateOf("")
    var category by mutableStateOf<FoodCategory?>(null)
    var quantityText by mutableStateOf("")
    var unit by mutableStateOf<MeasureUnit?>(null)

    val amountIsValid: Boolean
        get() = (quantityText.isBlank() && unit == null) || (parseQuantity(quantityText) != null && unit != null)

    val canSave: Boolean
        get() = loaded && name.isNotBlank() && category != null && amountIsValid

    init {
        if (isEditing) {
            viewModelScope.launch {
                dao.getById(itemId)?.let { item ->
                    original = item
                    name = item.name
                    category = item.category
                    quantityText = item.quantity?.let(::formatQuantity).orEmpty()
                    unit = item.unit
                }
                loaded = true
            }
        }
    }

    fun clearAmount() {
        quantityText = ""
        unit = null
    }

    fun save(onSaved: () -> Unit) {
        val category = category ?: return
        if (!canSave) return
        val quantity = parseQuantity(quantityText)
        val unit = unit.takeIf { quantity != null }
        viewModelScope.launch {
            val existing = original
            if (existing == null) {
                repository.addToGroceryList(name.trim(), category, amountOf(quantity, unit))
            } else {
                dao.update(existing.copy(name = name.trim(), category = category, quantity = quantity, unit = unit))
            }
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val existing = original ?: return
        viewModelScope.launch {
            dao.delete(existing)
            onDeleted()
        }
    }

    companion object {
        const val GROCERY_ID_ARG = "id"
        const val NEW_GROCERY_ID = -1L
    }
}
