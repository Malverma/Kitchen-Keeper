package com.kitchenkeeper.ui.recipe

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchenkeeper.data.RecipeWithIngredients
import com.kitchenkeeper.ui.AppViewModelProvider
import com.kitchenkeeper.ui.components.EmptyState
import com.kitchenkeeper.ui.components.PhotoThumbnail
import com.kitchenkeeper.ui.components.SwipeToDeleteRow
import com.kitchenkeeper.ui.components.showUndoDelete

@Composable
fun RecipesScreen(
    snackbarHostState: SnackbarHostState,
    onOpenRecipe: (Long) -> Unit,
    viewModel: RecipesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val list = recipes ?: return
    if (list.isEmpty()) {
        EmptyState("No recipes yet. Tap + to add a recipe.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp),
    ) {
        items(list, key = { it.recipe.id }) { recipe ->
            SwipeToDeleteRow(onDelete = {
                viewModel.delete(recipe)
                scope.showUndoDelete(
                    snackbarHostState,
                    recipe.recipe.name,
                    onUndo = { viewModel.undoDelete(recipe) },
                    onFinalize = { viewModel.finalizeDelete(recipe) },
                )
            }) {
                RecipeRow(recipe = recipe, onClick = { onOpenRecipe(recipe.recipe.id) })
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun RecipeRow(recipe: RecipeWithIngredients, onClick: () -> Unit) {
    val ingredients = recipe.ingredients
    val summary = when (ingredients.size) {
        0 -> "No ingredients"
        else -> ingredients.joinToString(", ") { it.name }
    }
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = recipe.recipe.photoPath?.let { path -> { PhotoThumbnail(path, recipe.recipe.name) } },
        headlineContent = { Text(recipe.recipe.name, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(summary, maxLines = 1, overflow = TextOverflow.Ellipsis) },
    )
}
