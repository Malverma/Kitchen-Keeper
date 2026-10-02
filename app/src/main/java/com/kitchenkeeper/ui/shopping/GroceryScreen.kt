package com.kitchenkeeper.ui.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchenkeeper.data.MeasureUnit
import com.kitchenkeeper.ui.AppViewModelProvider
import com.kitchenkeeper.ui.components.CategorySelector
import com.kitchenkeeper.ui.components.ConfirmDialog
import com.kitchenkeeper.ui.components.FormTopBar
import com.kitchenkeeper.ui.components.QuantityWithUnit
import com.kitchenkeeper.ui.components.SectionLabel

@Composable
fun GroceryScreen(
    onDone: () -> Unit,
    viewModel: GroceryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FormTopBar(
                title = if (viewModel.isEditing) "Edit Grocery Item" else "Add Grocery Item",
                onBack = onDone,
                canSave = viewModel.canSave,
                onSave = { viewModel.save(onDone) },
                onDelete = if (viewModel.isEditing && viewModel.loaded) {
                    { showDeleteDialog = true }
                } else {
                    null
                },
            )
        },
    ) { innerPadding ->
        if (!viewModel.loaded) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )

            CategorySelector(selected = viewModel.category, onSelect = { viewModel.category = it })

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { SectionLabel("Amount to buy (optional)") }
                    if (viewModel.quantityText.isNotEmpty() || viewModel.unit != null) {
                        TextButton(onClick = viewModel::clearAmount) { Text("Clear") }
                    }
                }
                QuantityWithUnit(
                    quantityText = viewModel.quantityText,
                    onQuantityChange = { viewModel.quantityText = it },
                    units = MeasureUnit.entries,
                    selectedUnit = viewModel.unit,
                    unitLabel = { it.label },
                    onUnitSelected = { viewModel.unit = it },
                    isError = !viewModel.amountIsValid && viewModel.quantityText.isNotBlank(),
                )
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete ${viewModel.name.ifBlank { "item" }}?",
            text = "This item will be removed from your grocery list.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete(onDone)
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}
