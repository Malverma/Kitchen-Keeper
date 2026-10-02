package com.kitchenkeeper.ui.shopping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchenkeeper.data.GroceryItem
import com.kitchenkeeper.data.amount
import com.kitchenkeeper.ui.AppViewModelProvider
import com.kitchenkeeper.ui.components.EmptyState
import com.kitchenkeeper.ui.components.SwipeToDeleteRow
import com.kitchenkeeper.ui.components.showUndoDelete
import kotlinx.coroutines.launch

@Composable
fun ShoppingScreen(
    snackbarHostState: SnackbarHostState,
    onEditGrocery: (Long) -> Unit,
    viewModel: ShoppingViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val list = items ?: return
    if (list.isEmpty()) {
        EmptyState("Your grocery list is empty. Tap + to add a grocery item.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp),
    ) {
        items(list, key = { it.id }) { item ->
            SwipeToDeleteRow(onDelete = {
                viewModel.delete(item)
                scope.showUndoDelete(
                    snackbarHostState,
                    item.name,
                    onUndo = { viewModel.undoDelete(item) },
                    onFinalize = {},
                )
            }) {
                GroceryRow(item = item, onClick = { onEditGrocery(item.id) }, onCheck = {
                    viewModel.checkOff(item)
                    snackbarHostState.currentSnackbarData?.dismiss()
                    scope.launch { snackbarHostState.showSnackbar("${item.name} added to pantry") }
                })
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun GroceryRow(item: GroceryItem, onClick: () -> Unit, onCheck: () -> Unit) {
    val details = listOfNotNull(item.category.label, item.amount?.formatted()).joinToString(" · ")
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { Checkbox(checked = false, onCheckedChange = { onCheck() }) },
        headlineContent = { Text(item.name, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(details) },
    )
}
