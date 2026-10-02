package com.kitchenkeeper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kitchenkeeper.data.AccentColor
import com.kitchenkeeper.data.ThemeMode
import com.kitchenkeeper.data.ThemeSettings
import com.kitchenkeeper.ui.pantry.PantryScreen
import com.kitchenkeeper.ui.recipe.RecipesScreen
import com.kitchenkeeper.ui.settings.SettingsDialog
import com.kitchenkeeper.ui.shopping.ShoppingScreen
import kotlinx.coroutines.launch

private enum class MainTab(val label: String, val addLabel: String) {
    PANTRY("Pantry", "Add food item"),
    COOKING("Cooking", "Add recipe"),
    SHOPPING("Shopping", "Add grocery item"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    themeSettings: ThemeSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAccentChange: (AccentColor) -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (Long) -> Unit,
    onAddRecipe: () -> Unit,
    onOpenRecipe: (Long) -> Unit,
    onAddGrocery: () -> Unit,
    onEditGrocery: (Long) -> Unit,
    message: String?,
    onMessageShown: () -> Unit,
) {
    val tabs = MainTab.entries
    // Always opens on the Pantry tab (index 0).
    val pagerState = rememberPagerState(initialPage = MainTab.PANTRY.ordinal) { tabs.size }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    if (showSettings) {
        SettingsDialog(
            settings = themeSettings,
            onModeChange = onThemeModeChange,
            onAccentChange = onAccentChange,
            onDismiss = { showSettings = false },
        )
    }

    LaunchedEffect(message) {
        if (message != null) {
            // Show from a scope that isn't restarted when the message is cleared below.
            scope.launch { snackbarHostState.showSnackbar(message) }
            onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.statusBarsPadding(),
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(tab.label) },
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            // Full-width row so settings sits bottom-left and the add button bottom-right.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SmallFloatingActionButton(onClick = { showSettings = true }) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
                val tab = tabs[pagerState.currentPage]
                FloatingActionButton(
                    onClick = when (tab) {
                        MainTab.PANTRY -> onAddItem
                        MainTab.COOKING -> onAddRecipe
                        MainTab.SHOPPING -> onAddGrocery
                    },
                ) {
                    Icon(Icons.Filled.Add, contentDescription = tab.addLabel)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { page ->
            when (tabs[page]) {
                MainTab.PANTRY -> PantryScreen(snackbarHostState = snackbarHostState, onEditItem = onEditItem)
                MainTab.COOKING -> RecipesScreen(snackbarHostState = snackbarHostState, onOpenRecipe = onOpenRecipe)
                MainTab.SHOPPING -> ShoppingScreen(snackbarHostState = snackbarHostState, onEditGrocery = onEditGrocery)
            }
        }
    }
}
