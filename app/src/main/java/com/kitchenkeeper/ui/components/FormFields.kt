package com.kitchenkeeper.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kitchenkeeper.data.FoodCategory

@Composable
fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTopBar(
    title: String,
    onBack: () -> Unit,
    canSave: Boolean,
    onSave: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                }
            }
            TextButton(onClick = onSave, enabled = canSave) { Text("Save") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(selected: FoodCategory?, onSelect: (FoodCategory) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("Category")
        val categories = FoodCategory.entries
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            categories.forEachIndexed { index, category ->
                SegmentedButton(
                    selected = selected == category,
                    onClick = { onSelect(category) },
                    shape = SegmentedButtonDefaults.itemShape(index, categories.size),
                    // Four segments don't leave room for the checkmark.
                    icon = {},
                ) {
                    Text(category.label, maxLines = 1, overflow = TextOverflow.Visible)
                }
            }
        }
    }
}

/** A quantity text field next to a unit dropdown. The quantity field is hidden when [showQuantity] is false. */
@Composable
fun <T> QuantityWithUnit(
    quantityText: String,
    onQuantityChange: (String) -> Unit,
    units: List<T>,
    selectedUnit: T?,
    unitLabel: (T) -> String,
    onUnitSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    showQuantity: Boolean = true,
    isError: Boolean = false,
    unitPlaceholder: String = "Unit",
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showQuantity) {
            OutlinedTextField(
                value = quantityText,
                onValueChange = onQuantityChange,
                label = { Text("Amount") },
                singleLine = true,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
        }
        UnitDropdown(
            units = units,
            selected = selectedUnit,
            label = unitLabel,
            onSelected = onUnitSelected,
            placeholder = unitPlaceholder,
            modifier = Modifier.weight(if (showQuantity) 1.3f else 1f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> UnitDropdown(
    units: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelected: (T) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected?.let(label).orEmpty(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(label(unit)) },
                    onClick = {
                        onSelected(unit)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
