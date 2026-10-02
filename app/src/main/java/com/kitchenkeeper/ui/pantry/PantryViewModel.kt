package com.kitchenkeeper.ui.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitchenkeeper.data.FoodItem
import com.kitchenkeeper.data.FoodItemDao
import com.kitchenkeeper.data.PhotoStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PantryViewModel(
    private val dao: FoodItemDao,
    private val photoStore: PhotoStore,
) : ViewModel() {

    /** Null until the first load completes, so the empty state doesn't flash on launch. */
    val items: StateFlow<List<FoodItem>?> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Removes the row but keeps the photo until the undo window closes. */
    fun delete(item: FoodItem) {
        viewModelScope.launch { dao.delete(item) }
    }

    fun undoDelete(item: FoodItem) {
        viewModelScope.launch { dao.insert(item) }
    }

    fun finalizeDelete(item: FoodItem) {
        photoStore.delete(item.photoPath)
    }
}
