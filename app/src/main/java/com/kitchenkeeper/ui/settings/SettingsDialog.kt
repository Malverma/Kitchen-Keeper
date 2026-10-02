package com.kitchenkeeper.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kitchenkeeper.data.AccentColor
import com.kitchenkeeper.data.ThemeMode
import com.kitchenkeeper.data.ThemeSettings

private const val SWATCHES_PER_ROW = 4

@Composable
fun SettingsDialog(
    settings: ThemeSettings,
    onModeChange: (ThemeMode) -> Unit,
    onAccentChange: (AccentColor) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Theme", style = MaterialTheme.typography.titleSmall)
                val modes = ThemeMode.entries
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    modes.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = settings.mode == mode,
                            onClick = { onModeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                            label = { Text(mode.label) },
                        )
                    }
                }

                Text("Color", style = MaterialTheme.typography.titleSmall)
                AccentColor.entries.chunked(SWATCHES_PER_ROW).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        row.forEach { accent ->
                            ColorSwatch(
                                accent = accent,
                                selected = settings.accent == accent,
                                onClick = { onAccentChange(accent) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
    )
}

@Composable
private fun ColorSwatch(accent: AccentColor, selected: Boolean, onClick: () -> Unit) {
    val color = Color(accent.seed)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                else Modifier,
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = accent.label
                this.selected = selected
            },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
    }
}
