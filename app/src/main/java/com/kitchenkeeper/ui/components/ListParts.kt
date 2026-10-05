package com.kitchenkeeper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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

/** Swipe right to delete, swipe left to edit. */
@Composable
fun SwipeActionsRow(onDelete: () -> Unit, onEdit: () -> Unit, content: @Composable () -> Unit) {
    // Act when the swipe completes but never stay dismissed: the row's swipe state is restored by
    // key, so a dismissed state would re-delete the row as soon as Undo brings it back.
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> onDelete()
                SwipeToDismissBoxValue.EndToStart -> onEdit()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val editing = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (editing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    )
                    .padding(horizontal = 24.dp),
                contentAlignment = if (editing) Alignment.CenterEnd else Alignment.CenterStart,
            ) {
                if (editing) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                } else {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
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
        // SnackbarDuration.Short is fixed at 4s, so time the snackbar ourselves; timing out
        // cancels showSnackbar, which hides it.
        val result = withTimeoutOrNull(UNDO_TIMEOUT_MS) {
            snackbarHostState.showSnackbar(
                message = "$name deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Indefinite,
            )
        }
        if (result == SnackbarResult.ActionPerformed) onUndo() else onFinalize()
    }
}

private const val UNDO_TIMEOUT_MS = 5_000L
