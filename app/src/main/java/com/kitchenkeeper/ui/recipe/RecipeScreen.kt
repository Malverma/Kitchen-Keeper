package com.kitchenkeeper.ui.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchenkeeper.data.FoodItem
import com.kitchenkeeper.data.UsageUnit
import com.kitchenkeeper.data.amount
import com.kitchenkeeper.ui.AppViewModelProvider
import com.kitchenkeeper.ui.components.ConfirmDialog
import com.kitchenkeeper.ui.components.FormTopBar
import com.kitchenkeeper.ui.components.PhotoSection
import com.kitchenkeeper.ui.components.QuantityWithUnit
import com.kitchenkeeper.ui.components.SectionLabel

@Composable
fun RecipeScreen(
    onDone: (message: String?) -> Unit,
    viewModel: RecipeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val pantry by viewModel.pantry.collectAsStateWithLifecycle()
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var showCookDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            FormTopBar(
                title = if (viewModel.isEditing) "Edit Recipe" else "Add Recipe",
                onBack = { onDone(null) },
                canSave = viewModel.canSave,
                onSave = { viewModel.save { onDone(null) } },
                onDelete = if (viewModel.isEditing && viewModel.loaded) {
                    { showDeleteDialog = true }
                } else {
                    null
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = { showCookDialog = true },
                    enabled = viewModel.canCook,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                ) {
                    Text("Cook Meal")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                label = { Text("Recipe name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )

            PhotoSection(session = viewModel.photo, snackbarHostState = snackbarHostState)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Ingredients from pantry")
                viewModel.ingredients.forEachIndexed { index, draft ->
                    IngredientCard(
                        draft = draft,
                        pantryItem = draft.findIn(pantry),
                        onChange = { viewModel.updateIngredient(index, it) },
                        onRemove = { viewModel.removeIngredient(index) },
                    )
                }
                OutlinedButton(onClick = { showPicker = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("Add ingredient", Modifier.padding(start = 8.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Instructions")
                OutlinedTextField(
                    value = viewModel.instructions,
                    onValueChange = { viewModel.instructions = it },
                    placeholder = { Text("How to cook this meal") },
                    minLines = 5,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showPicker) {
        val addedIds = viewModel.ingredients.mapNotNull { it.foodItemId }.toSet()
        val addedNames = viewModel.ingredients.map { it.name.lowercase() }.toSet()
        PantryPickerDialog(
            items = pantry.filter { it.id !in addedIds && it.name.lowercase() !in addedNames },
            pantryIsEmpty = pantry.isEmpty(),
            onPick = {
                viewModel.addIngredient(it)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }

    if (showCookDialog) {
        CookDialog(
            recipeName = viewModel.name.trim(),
            ingredients = viewModel.ingredients,
            onConfirm = {
                showCookDialog = false
                viewModel.cook { recipeName, count ->
                    val items = if (count == 1) "1 item" else "$count items"
                    onDone("Cooked $recipeName. $items added to the grocery list.")
                }
            },
            onDismiss = { showCookDialog = false },
        )
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete ${viewModel.name.ifBlank { "recipe" }}?",
            text = "This recipe will be removed. Your pantry won't change.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete { onDone(null) }
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun IngredientCard(
    draft: IngredientDraft,
    pantryItem: FoodItem?,
    onChange: (IngredientDraft) -> Unit,
    onRemove: () -> Unit,
) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(top = 12.dp),
                ) {
                    Text(draft.name, style = MaterialTheme.typography.titleMedium)
                    val (availability, color) = when {
                        pantryItem == null -> "Not in pantry" to MaterialTheme.colorScheme.error
                        else -> {
                            val onHand = pantryItem.amount?.formatted()
                            (if (onHand != null) "In pantry: $onHand" else "In pantry") to
                                MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    }
                    Text(availability, style = MaterialTheme.typography.bodySmall, color = color)
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Outlined.Close, contentDescription = "Remove ${draft.name}")
                }
            }
            QuantityWithUnit(
                quantityText = draft.quantityText,
                onQuantityChange = { onChange(draft.copy(quantityText = it)) },
                units = UsageUnit.entries,
                selectedUnit = draft.unit,
                unitLabel = { it.label },
                onUnitSelected = { onChange(draft.copy(unit = it)) },
                showQuantity = draft.unit.needsQuantity,
                isError = !draft.isValid && draft.quantityText.isNotBlank(),
                unitPlaceholder = if (draft.unit.needsQuantity) "Unit" else "Amount used",
                modifier = Modifier.padding(top = 8.dp, end = 12.dp),
            )
        }
    }
}

@Composable
private fun PantryPickerDialog(
    items: List<FoodItem>,
    pantryIsEmpty: Boolean,
    onPick: (FoodItem) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add from pantry") },
        text = {
            when {
                pantryIsEmpty -> Text("Your pantry is empty. Add food items on the Pantry tab first.")
                items.isEmpty() -> Text("Every pantry item is already in this recipe.")
                else -> LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    items(items, key = { it.id }) { item ->
                        ListItem(
                            modifier = Modifier.clickable { onPick(item) },
                            headlineContent = { Text(item.name) },
                            supportingContent = {
                                Text(listOfNotNull(item.category.label, item.amount?.formatted()).joinToString(" · "))
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun CookDialog(
    recipeName: String,
    ingredients: List<IngredientDraft>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cook $recipeName?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("These will be removed from your pantry and added to your grocery list:")
                ingredients.forEach {
                    Text("•  ${it.name} — ${it.usageLabel()}")
                }
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Cook") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
