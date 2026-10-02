package com.kitchenkeeper.ui.item

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchenkeeper.data.MeasureUnit
import com.kitchenkeeper.ui.AppViewModelProvider
import com.kitchenkeeper.ui.components.CategorySelector
import com.kitchenkeeper.ui.components.ConfirmDialog
import com.kitchenkeeper.ui.components.FormTopBar
import com.kitchenkeeper.ui.components.PhotoSection
import com.kitchenkeeper.ui.components.QuantityWithUnit
import com.kitchenkeeper.ui.components.SectionLabel
import com.kitchenkeeper.ui.components.formatted
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun ItemScreen(
    onDone: () -> Unit,
    viewModel: ItemViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            FormTopBar(
                title = if (viewModel.isEditing) "Edit Food Item" else "Add Food Item",
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
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )

            PhotoSection(session = viewModel.photo, snackbarHostState = snackbarHostState)

            CategorySelector(selected = viewModel.category, onSelect = { viewModel.category = it })

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { SectionLabel("Amount on hand (optional)") }
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

            ExpirationDateField(
                date = viewModel.expirationDate,
                onClick = { showDatePicker = true },
                onClear = { viewModel.expirationDate = null },
            )
        }
    }

    if (showDatePicker) {
        ExpirationDatePickerDialog(
            initial = viewModel.expirationDate,
            onConfirm = {
                viewModel.expirationDate = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete ${viewModel.name.ifBlank { "item" }}?",
            text = "This item will be removed from your pantry.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete(onDone)
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun ExpirationDateField(date: LocalDate?, onClick: () -> Unit, onClear: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("Expiration date (optional)")
        Box {
            OutlinedTextField(
                value = date?.formatted() ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("No expiration date") },
                leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                trailingIcon = if (date != null) {
                    {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Outlined.Clear, contentDescription = "Clear expiration date")
                        }
                    }
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth(),
            )
            // A read-only text field swallows taps, so overlay a click target
            // that leaves the clear button exposed.
            Box(
                Modifier
                    .matchParentSize()
                    .padding(end = if (date != null) 48.dp else 0.dp)
                    .clickable(onClick = onClick),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpirationDatePickerDialog(
    initial: LocalDate?,
    onConfirm: (LocalDate?) -> Unit,
    onDismiss: () -> Unit,
) {
    // DatePicker works in UTC-midnight millis.
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    state.selectedDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    },
                )
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = state)
    }
}
