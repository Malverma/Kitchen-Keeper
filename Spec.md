# Kitchen Keeper — Specification

## 1. Overview

Kitchen Keeper is an Android app for keeping track of the food in your kitchen. It has three tabs:

- **Pantry**: the food you have, and how much of it.
- **Cooking**: your recipes. Cooking a meal uses up its ingredients from the pantry.
- **Shopping**: a grocery list. It fills up automatically with whatever you cook with, so you can buy it again.

The three tabs form a loop. Cooking takes food out of the pantry and puts it on the grocery list. Checking an item off the grocery list puts it back in the pantry.

## 2. Platform & Tech Stack

| Concern            | Choice                                                        |
| ------------------ | ------------------------------------------------------------- |
| Platform           | Android                                                       |
| Min SDK            | 26 (Android 8.0)                                              |
| Target SDK         | 35                                                            |
| Language           | Kotlin                                                        |
| UI                 | Jetpack Compose + Material 3                                  |
| Architecture       | MVVM (ViewModel + StateFlow), single-activity, Navigation Compose |
| Local storage      | Room (SQLite), schema exported to `app/schemas/`              |
| Camera             | `ActivityResultContracts.TakePicture` + `FileProvider`        |
| Image loading      | Coil                                                          |
| Date input         | Material 3 `DatePicker`                                       |

All data is stored on the device. There is no account, login, or network sync.

## 3. App Structure & Navigation

### 3.1 Top Tab Bar

- A tab bar at the **very top of the screen**, directly below the status bar. There is no app title bar.
- It has three tabs, in this order and with these exact labels: **Pantry**, **Cooking**, **Shopping**.
- Tapping a tab switches to it. Swiping left or right between tabs also works.
- When the app opens it **always starts on the Pantry tab**.

### 3.2 Add Button

- Every tab has a `+` Floating Action Button (FAB) in the **bottom-right** corner. What it adds depends on the tab:

| Tab      | FAB opens              |
| -------- | ---------------------- |
| Pantry   | Add Food Item (§4.3)   |
| Cooking  | Add Recipe (§5.2)      |
| Shopping | Add Grocery Item (§6.2) |

### 3.3 Settings

- A small settings (gear) button sits in the **bottom-left** corner of the main screen, opposite the `+` FAB.
- It opens a **Settings** dialog with:
  - **Theme:** Light or Dark. The default is Light.
  - **Color:** 12 primary colors (Green, Teal, Cyan, Blue, Indigo, Purple, Pink, Red, Orange, Amber, Brown, Slate) shown as swatches. The default is Green. The whole app's color scheme is built from the chosen color.
- Changes apply immediately and are saved on the device (SharedPreferences), so they survive a restart.

### 3.4 Shared Conventions

- **Categories:** every food item and grocery item has exactly one category: **Meats**, **Vegetables**, **Grains** or **Misc**. Categories appear only as text. No icons are used for them.
- **Photos:** a photo is optional on food items and recipes. Rows without a photo show no image at all.
- **Swipe to delete:** on every list, swiping a row left or right deletes it. A snackbar then shows *"<Name> deleted"* with an **Undo** action. Only one Undo is offered at a time.
- **Sorting:** every list is sorted alphabetically by name, ignoring case.

## 4. Pantry Tab

### 4.1 Pantry List

Each row shows:
- **Photo** thumbnail, if the item has one.
- **Name**.
- A details line: **category**, the **amount on hand** if one is set (e.g. `2 lb`), and the **expiration date** if one is set. For example: `Meats · 2 lb · Exp: Oct 14, 2026`.

Tapping a row opens it in Edit Food Item (§4.4).

**Empty state:** *"Your pantry is empty. Tap + to add a food item."*

### 4.2 Units of Measure

The pantry and the grocery list measure amounts in these units:

| Unit        | Label       |
| ----------- | ----------- |
| `LB`        | lb          |
| `G`         | g           |
| `CUP`       | cup / cups  |
| `CONTAINER` | container / containers |

Pounds and grams convert into each other (1 lb = 453.59237 g). No other pair of units can be converted. Quantities are rounded to 2 decimal places.

### 4.3 Add Food Item Screen

| Field           | Input                                       | Required | Notes |
| --------------- | ------------------------------------------- | -------- | ----- |
| Name            | Text field                                  | Yes      | Trimmed. |
| Photo           | Tap to open the camera                      | No       | Can be retaken or removed after capture. |
| Category        | Segmented buttons: Meats / Vegetables / Grains / Misc | Yes | None selected by default. |
| Amount on hand  | Number field + unit dropdown (lb, g, cup, container) | No | If either part is filled in, both must be valid, and the number must be greater than 0. A **Clear** button empties both. |
| Expiration date | Date picker field                           | No       | Has a clear (✕) button. |

- **Save** is disabled until the required fields are filled in and any amount entered is valid.
- Leaving without saving throws the input away, including any photos taken.

### 4.4 Edit Food Item Screen

- Uses the same form as §4.3, with every field filled in from the saved item. The title is "Edit Food Item".
- **Save** updates the existing item and does not create a new one.
- A **Delete** (trash) action asks *"Delete <Name>?"* for confirmation, then removes the item and its photo.

### 4.5 Photos

- **Take Photo** opens the camera. The photo is saved to `filesDir/images/<uuid>.jpg`.
- The camera permission is requested the first time Take Photo is tapped. If it is denied, a snackbar explains that a photo is optional.
- No photo file is ever left behind unused:
  - A photo taken but never saved is deleted when the user leaves the screen.
  - A photo that is replaced or removed is deleted once the change is saved.
  - Deleting an item deletes its photo. After a swipe-delete, the photo is deleted once the Undo window closes.

## 5. Cooking Tab

### 5.1 Recipe List

- Each row shows the recipe's photo (if it has one), its **name**, and its ingredient names separated by commas.
- Tapping a row opens the recipe (§5.2).
- **Empty state:** *"No recipes yet. Tap + to add a recipe."*

### 5.2 Add / Edit Recipe Screen

| Field        | Input | Required | Notes |
| ------------ | ----- | -------- | ----- |
| Recipe name  | Text field | Yes | |
| Photo        | Tap to open the camera | No | Same behavior as §4.5. |
| Ingredients  | **Add ingredient** button, then a list of ingredient cards | No (but needed to cook) | See below. |
| Instructions | Multi-line text field | No | How to cook the meal. |

**Adding an ingredient:**
- **Add ingredient** opens a list of the items in the pantry, showing each item's name, category and amount. Items already in the recipe are left out.
- Tapping an item adds it to the recipe as an ingredient card.

**Each ingredient card shows:**
- The ingredient's **name**, and whether the pantry has it: `In pantry: 2 lb`, `In pantry`, or `Not in pantry` (in red).
- **Amount used:** a dropdown with **LB**, **G**, **Cup**, **Half container** and **Full container**.
  - For LB, G and Cup, an **Amount** number field also appears. It is required and must be greater than 0.
  - Half container and Full container need no number. They count as 0.5 and 1 container.
  - The default choice follows the pantry item's unit: lb → LB, g → G, cup → Cup, and container or no unit → Full container.
- A remove (✕) button.

**Actions:**
- **Save** is in the app bar. It is enabled once the recipe has a name and every ingredient's amount is valid.
- **Delete** is in the app bar when editing. It asks for confirmation first. Deleting a recipe never changes the pantry.
- **Cook Meal** is a full-width button pinned to the bottom of the screen (§5.3). It is enabled when Save would be enabled and the recipe has at least one ingredient.

### 5.3 Cook Meal

1. A confirmation dialog appears: *"Cook <Recipe>?"* It lists every ingredient and the amount used (e.g. `Chicken — 1 lb`, `Rice — Half container`).
2. On confirm, the recipe is saved first (with any unsaved edits), then cooked:
   - **Pantry:** for each ingredient, find the pantry item it was added from. If that item is gone, look for a pantry item with the same name, ignoring case.
     - If the pantry item has an amount and the units can be compared, subtract the amount used. When the result reaches 0 or less, remove the item (and its photo).
     - If the pantry item has no amount, or the units can't be compared (e.g. Cup vs. LB), remove the item.
     - If no matching pantry item exists, the pantry is not changed.
   - **Grocery list:** every ingredient is added with the amount used (§6.3), whether or not it was in the pantry.
   - All changes happen in one database transaction.
3. The screen closes and the main screen shows a snackbar: *"Cooked <Recipe>. N items added to the grocery list."*
4. The recipe stays in the list so it can be cooked again.

## 6. Shopping Tab

### 6.1 Grocery List

- Each row shows a **checkbox**, the **name**, and a details line with the category and the amount to buy, e.g. `Meats · 2 lb`.
- **Checking the box** marks the item as bought (§6.4) and shows *"<Name> added to pantry"*.
- **Tapping a row** opens it in Edit Grocery Item (§6.2).
- **Swipe** deletes the item without adding it to the pantry. Undo is available.
- **Empty state:** *"Your grocery list is empty. Tap + to add a grocery item."*

### 6.2 Add / Edit Grocery Item Screen

- Editing uses the same form with every field filled in. The title is "Edit Grocery Item".
- When editing, a **Delete** (trash) action asks *"Delete <Name>?"* for confirmation, then removes the item from the grocery list without changing the pantry.

| Field         | Input | Required |
| ------------- | ----- | -------- |
| Name          | Text field | Yes |
| Category      | Meats / Vegetables / Grains / Misc | Yes |
| Amount to buy | Number field + unit dropdown (lb, g, cup, container) | No (if one part is filled in, both must be) |

### 6.3 Merging Duplicates

When an item is added to the grocery list, either by cooking or by hand:
- If the list already has an entry with the same name (ignoring case) and a unit that can be converted, the new amount is added to that entry. For example, cooking a recipe that uses 1 lb of Chicken twice gives one entry: `Chicken · 2 lb`.
- Otherwise a new entry is created. One exception: an item added with no amount is skipped if an entry with that name and no amount already exists.

### 6.4 Checking Off an Item (Restock)

When an item is checked off, it moves into the pantry and is removed from the grocery list, all in one transaction:
- If the pantry has an item with the same name and a unit that can be converted, the bought amount is added to it.
- Otherwise a new pantry item is created with the grocery item's name, category and amount. It has no photo and no expiration date.

## 7. Data Model

Database version **2**. The `1 → 2` migration adds `quantity` and `unit` columns to `food_items` and creates the `recipes`, `recipe_ingredients` and `grocery_items` tables. Existing pantry data is kept.

```kotlin
enum class FoodCategory { MEATS, VEGETABLES, GRAINS, MISC }
enum class MeasureUnit { LB, G, CUP, CONTAINER }
enum class UsageUnit { LB, G, CUP, HALF_CONTAINER, FULL_CONTAINER }

@Entity("food_items")
data class FoodItem(
    id: Long, name: String, category: FoodCategory,
    photoPath: String?, expirationDate: LocalDate?, createdAt: Instant,
    quantity: Double?, unit: MeasureUnit?,   // both set or both null
)

@Entity("recipes")
data class Recipe(id: Long, name: String, photoPath: String?, instructions: String, createdAt: Instant)

@Entity("recipe_ingredients")  // FK recipeId → recipes.id, ON DELETE CASCADE
data class RecipeIngredient(
    id: Long, recipeId: Long,
    foodItemId: Long?,              // pantry item it was picked from (may no longer exist)
    name: String, category: FoodCategory,  // copied from the pantry item, so the recipe survives it being used up
    amount: Double?,                // null for Half/Full container
    unit: UsageUnit,
)

@Entity("grocery_items")
data class GroceryItem(id: Long, name: String, category: FoodCategory, quantity: Double?, unit: MeasureUnit?, createdAt: Instant)
```

`KitchenRepository` handles every operation that touches more than one table: `saveRecipe`, `deleteRecipe`/`restoreRecipe`, `cook`, `addToGroceryList` and `restock`.

## 8. Permissions

- `android.permission.CAMERA`: requested at runtime the first time Take Photo is tapped.
- No storage permissions are needed, because photos are kept in app-private storage.
- `android.hardware.camera` is declared with `required="false"`, so devices without a camera can still install the app.

## 9. Out of Scope

- Searching or filtering lists
- Expiration reminders and notifications
- Picking photos from the gallery, and photos on grocery items
- Barcode scanning
- Recipe servings or scaling
- Converting between volume and weight (e.g. cups ↔ lb)
- Cloud sync and user accounts

## 10. Acceptance Criteria

**Navigation**
1. The app opens on the Pantry tab. Three tabs labeled "Pantry", "Cooking" and "Shopping" appear at the top of the screen, with no title bar above them.
2. Each tab has a bottom-right `+` FAB that opens that tab's add screen.
2a. A bottom-left settings button switches between Light and Dark and among 12 primary colors. The choice survives a restart.

**Pantry**
3. A food item can be added with a name, an optional photo, one of four categories (Meats, Vegetables, Grains, Misc), an optional amount with a unit, and an optional expiration date.
4. Rows show the name, the category as text, the amount and expiration date if set, and the photo if there is one. No category icons appear.
5. Tapping an item opens it for editing with every field filled in. Saving updates that item and creates no duplicate.
6. Swiping deletes an item with Undo. Deleting from the edit screen asks for confirmation first.

**Cooking**
7. A recipe can be added with a name, an optional photo, ingredients picked from the pantry, and instructions.
8. Each ingredient has an amount-used dropdown (LB, G, Cup, Half container, Full container). LB, G and Cup also need a number.
9. The Cook Meal button sits at the bottom of the recipe screen and asks for confirmation before cooking.
10. Cooking subtracts each amount used from the pantry. Example: Chicken 2 lb minus 1 lb leaves 1 lb. Rice 1 container minus a half container leaves 0.5 container.
11. A pantry item that reaches 0, has no amount, or has a unit that can't be compared is removed from the pantry.
12. Every ingredient used is added to the grocery list with the amount used. Cooking twice merges the amounts (1 lb + 1 lb = 2 lb).

**Shopping**
13. The grocery list shows everything used in cooking, plus items added by hand with the `+` button.
14. Checking off an item removes it from the list and adds it back to the pantry, merging with a matching pantry item when the units can be compared.
15. Swiping a grocery item deletes it without changing the pantry. Undo is available.
15a. Tapping a grocery item opens it for editing, so its amount can be changed. Deleting from the edit screen asks for confirmation first.

**Persistence**
16. All data survives an app restart. Upgrading from database version 1 keeps existing pantry items.
