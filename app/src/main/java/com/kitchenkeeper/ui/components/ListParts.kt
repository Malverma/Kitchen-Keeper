package com.kitchenkeeper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PhotoThumbnail(path: String, contentDescription: String) {
    AsyncImage(
        model = File(path),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp)),
    )
}

@Composable
fun SwipeToDeleteRow(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) onDelete()
    }
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val alignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Alignment.CenterEnd
            } else {
                Alignment.CenterStart
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment,
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        content()
    }
}

/**
 * Shows "<name> deleted" with Undo. Only one undo is offered at a time: dismissing the
 * previous snackbar finalizes its delete.
 */
fun CoroutineScope.showUndoDelete(
    snackbarHostState: SnackbarHostState,
    name: String,
    onUndo: () -> Unit,
    onFinalize: () -> Unit,
) {
    snackbarHostState.currentSnackbarData?.dismiss()
    launch {
        val result = snackbarHostState.showSnackbar(
            message = "$name deleted",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) onUndo() else onFinalize()
    }
}
