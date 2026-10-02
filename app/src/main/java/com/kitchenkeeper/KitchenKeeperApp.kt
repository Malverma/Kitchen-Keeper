package com.kitchenkeeper

import android.app.Application
import com.kitchenkeeper.data.KitchenKeeperDatabase
import com.kitchenkeeper.data.KitchenRepository
import com.kitchenkeeper.data.PhotoStore
import com.kitchenkeeper.data.ThemeSettingsStore

class KitchenKeeperApp : Application() {
    val database by lazy { KitchenKeeperDatabase.create(this) }
    val photoStore by lazy { PhotoStore(this) }
    val repository by lazy { KitchenRepository(database, photoStore) }
    val themeSettings by lazy { ThemeSettingsStore(this) }
}
