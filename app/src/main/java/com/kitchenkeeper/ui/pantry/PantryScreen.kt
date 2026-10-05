package com.kitchenkeeper.ui.pantry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.kitchenkeeper.data.FoodItem
import com.kitchenkeeper.data.amount
import com.kitchenkeeper.ui.AppViewModelProvider
import com.kitchenkeeper.ui.components.EmptyState
import com.kitchenkeeper.ui.components.PhotoThumbnail
import com.kitchenkeeper.ui.components.SwipeActionsRow
import com.kitchenkeeper.ui.components.formatted
import com.kitchenkeeper.ui.components.showUndoDelete

@Composable
fun PantryScreen(
    snackbarHostState: SnackbarHostState,
    onEditItem: (Long) -> Unit,
    viewModel: PantryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val list = items ?: return
    if (list.isEmpty()) {
        EmptyState("Your pantry is empty. Tap + to add a food item.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Leave room so the FAB never covers the last row.
        contentPadding = PaddingValues(bottom = 88.dp),
    ) {
        items(list, key = { it.id }) { item ->
            SwipeActionsRow(
                onDelete = {
                    viewModel.delete(item)
                    scope.showUndoDelete(
                        snackbarHostState,
                        item.name,
                        onUndo = { viewModel.undoDelete(item) },
                        onFinalize = { viewModel.finalizeDelete(item) },
                    )
                },
                onEdit = { onEditItem(item.id) },
            ) {
                PantryRow(item = item, onClick = { onEditItem(item.id) })
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun PantryRow(item: FoodItem, onClick: () -> Unit) {
    val details = listOfNotNull(
        item.category.label,
        item.amount?.formatted(),
        item.expirationDate?.let { "Exp: ${it.formatted()}" },
    ).joinToString(" · ")
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = item.photoPath?.let { path -> { PhotoThumbnail(path, item.name) } },
        headlineContent = { Text(item.name, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(details) },
    )
}
