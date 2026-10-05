package com.kitchenkeeper.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitchenkeeper.data.GroceryItem
import com.kitchenkeeper.data.GroceryItemDao
import com.kitchenkeeper.data.KitchenRepository
import com.kitchenkeeper.data.PhotoStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingViewModel(
    private val dao: GroceryItemDao,
    private val repository: KitchenRepository,
    private val photoStore: PhotoStore,
) : ViewModel() {

    val items: StateFlow<List<GroceryItem>?> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Bought: moves the item into the pantry. */
    fun checkOff(item: GroceryItem) {
        viewModelScope.launch { repository.restock(item) }
    }

    /** Removes the row but keeps the photo until the undo window closes. */
    fun delete(item: GroceryItem) {
        viewModelScope.launch { dao.delete(item) }
    }

    fun undoDelete(item: GroceryItem) {
        viewModelScope.launch { dao.insert(item) }
    }

    fun finalizeDelete(item: GroceryItem) {
        photoStore.delete(item.photoPath)
    }
}
