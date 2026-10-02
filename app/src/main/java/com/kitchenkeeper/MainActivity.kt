package com.kitchenkeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kitchenkeeper.data.ThemeMode
import com.kitchenkeeper.data.ThemeSettingsStore
import com.kitchenkeeper.ui.MainScreen
import com.kitchenkeeper.ui.item.ItemScreen
import com.kitchenkeeper.ui.item.ItemViewModel
import com.kitchenkeeper.ui.recipe.RecipeScreen
import com.kitchenkeeper.ui.recipe.RecipeViewModel
import com.kitchenkeeper.ui.shopping.GroceryScreen
import com.kitchenkeeper.ui.shopping.GroceryViewModel
import com.kitchenkeeper.ui.theme.KitchenKeeperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themeSettings = (application as KitchenKeeperApp).themeSettings
        setContent {
            val settings by themeSettings.settings.collectAsStateWithLifecycle()
            val darkTheme = settings.mode == ThemeMode.DARK
            // Keep status/navigation bar icons readable when the app overrides the system theme.
            DisposableEffect(darkTheme) {
                val transparent = Color.Transparent.toArgb()
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(transparent, transparent) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(transparent, transparent) { darkTheme },
                )
                onDispose {}
            }
            KitchenKeeperTheme(darkTheme = darkTheme, accent = settings.accent) {
                KitchenKeeperNavHost(themeSettings)
            }
        }
    }
}

private const val MAIN_ROUTE = "main"
private const val ITEM_ROUTE = "item"
private const val RECIPE_ROUTE = "recipe"
private const val GROCERY_ROUTE = "grocery"

/** Key for a one-shot snackbar message a screen hands back to the main screen. */
private const val MESSAGE_KEY = "message"

@Composable
private fun KitchenKeeperNavHost(themeSettings: ThemeSettingsStore) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = MAIN_ROUTE) {
        composable(MAIN_ROUTE) { entry ->
            val message by entry.savedStateHandle.getStateFlow<String?>(MESSAGE_KEY, null)
                .collectAsStateWithLifecycle()
            val settings by themeSettings.settings.collectAsStateWithLifecycle()
            MainScreen(
                themeSettings = settings,
                onThemeModeChange = themeSettings::setMode,
                onAccentChange = themeSettings::setAccent,
                onAddItem = { navController.navigate(ITEM_ROUTE) },
                onEditItem = { id -> navController.navigate("$ITEM_ROUTE?${ItemViewModel.ITEM_ID_ARG}=$id") },
                onAddRecipe = { navController.navigate(RECIPE_ROUTE) },
                onOpenRecipe = { id ->
                    navController.navigate("$RECIPE_ROUTE?${RecipeViewModel.RECIPE_ID_ARG}=$id")
                },
                onAddGrocery = { navController.navigate(GROCERY_ROUTE) },
                onEditGrocery = { id ->
                    navController.navigate("$GROCERY_ROUTE?${GroceryViewModel.GROCERY_ID_ARG}=$id")
                },
                message = message,
                onMessageShown = { entry.savedStateHandle[MESSAGE_KEY] = null },
            )
        }
        composable(
            route = "$ITEM_ROUTE?${ItemViewModel.ITEM_ID_ARG}={${ItemViewModel.ITEM_ID_ARG}}",
            arguments = listOf(
                navArgument(ItemViewModel.ITEM_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = ItemViewModel.NEW_ITEM_ID
                },
            ),
        ) { entry ->
            ItemScreen(onDone = { navController.popIfCurrent(entry) })
        }
        composable(
            route = "$RECIPE_ROUTE?${RecipeViewModel.RECIPE_ID_ARG}={${RecipeViewModel.RECIPE_ID_ARG}}",
            arguments = listOf(
                navArgument(RecipeViewModel.RECIPE_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = RecipeViewModel.NEW_RECIPE_ID
                },
            ),
        ) { entry ->
            RecipeScreen(onDone = { message ->
                if (navController.currentBackStackEntry?.id == entry.id && message != null) {
                    navController.previousBackStackEntry?.savedStateHandle?.set(MESSAGE_KEY, message)
                }
                navController.popIfCurrent(entry)
            })
        }
        composable(
            route = "$GROCERY_ROUTE?${GroceryViewModel.GROCERY_ID_ARG}={${GroceryViewModel.GROCERY_ID_ARG}}",
            arguments = listOf(
                navArgument(GroceryViewModel.GROCERY_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = GroceryViewModel.NEW_GROCERY_ID
                },
            ),
        ) { entry ->
            GroceryScreen(onDone = { navController.popIfCurrent(entry) })
        }
    }
}

/**
 * Pops [entry] only while it is still the top screen. Saves finish asynchronously and Back/Save
 * can race, so a late or repeated "done" must not pop the screen underneath (leaving a blank app).
 */
private fun NavController.popIfCurrent(entry: NavBackStackEntry) {
    if (currentBackStackEntry?.id == entry.id) popBackStack()
}
