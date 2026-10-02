package com.kitchenkeeper.ui.item

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitchenkeeper.data.FoodCategory
import com.kitchenkeeper.data.FoodItem
import com.kitchenkeeper.data.FoodItemDao
import com.kitchenkeeper.data.MeasureUnit
import com.kitchenkeeper.data.PhotoStore
import com.kitchenkeeper.data.formatQuantity
import com.kitchenkeeper.ui.components.PhotoSession
import com.kitchenkeeper.ui.components.parseQuantity
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

/** Backs both the Add and Edit Food Item screens. */
class ItemViewModel(
    savedStateHandle: SavedStateHandle,
    private val dao: FoodItemDao,
    photoStore: PhotoStore,
) : ViewModel() {

    private val itemId: Long = savedStateHandle[ITEM_ID_ARG] ?: NEW_ITEM_ID
    val isEditing: Boolean = itemId != NEW_ITEM_ID

    private var original: FoodItem? = null
    val photo = PhotoSession(photoStore)

    var loaded by mutableStateOf(!isEditing)
        private set
    var name by mutableStateOf("")
    var category by mutableStateOf<FoodCategory?>(null)
    var quantityText by mutableStateOf("")
    var unit by mutableStateOf<MeasureUnit?>(null)
    var expirationDate by mutableStateOf<LocalDate?>(null)

    /** Amount is optional, but if either half is filled in, both must be valid. */
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
                    photo.load(item.photoPath)
                    quantityText = item.quantity?.let(::formatQuantity).orEmpty()
                    unit = item.unit
                    expirationDate = item.expirationDate
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
                dao.insert(
                    FoodItem(
                        name = name.trim(),
                        category = category,
                        photoPath = photo.photoPath,
                        expirationDate = expirationDate,
                        createdAt = Instant.now(),
                        quantity = quantity,
                        unit = unit,
                    ),
                )
            } else {
                dao.update(
                    existing.copy(
                        name = name.trim(),
                        category = category,
                        photoPath = photo.photoPath,
                        expirationDate = expirationDate,
                        quantity = quantity,
                        unit = unit,
                    ),
                )
            }
            photo.commit()
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val existing = original ?: return
        viewModelScope.launch {
            dao.delete(existing)
            photo.deleteAll()
            onDeleted()
        }
    }

    /** Leaving without saving: drop any photos taken during this visit. */
    override fun onCleared() {
        photo.discard()
    }

    companion object {
        const val ITEM_ID_ARG = "id"
        const val NEW_ITEM_ID = -1L
    }
}
