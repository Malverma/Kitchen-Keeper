package com.kitchenkeeper.ui.components

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kitchenkeeper.data.PhotoStore
import java.io.File

/**
 * Tracks the photo on an add/edit form so files never leak: photos taken but not kept are
 * deleted, and a replaced photo is deleted only once the new one is saved. Lives in a ViewModel.
 */
class PhotoSession(private val store: PhotoStore) {
    var photoPath by mutableStateOf<String?>(null)
        private set

    private var savedPath: String? = null
    private val unsaved = mutableListOf<String>()

    /** File handed to the camera; kept here so it survives activity recreation. */
    private var pending: File? = null

    fun load(path: String?) {
        savedPath = path
        photoPath = path
    }

    fun prepareCapture(): Uri {
        val file = store.newPhotoFile()
        pending = file
        unsaved += file.path
        return store.uriFor(file)
    }

    fun onCaptureResult(success: Boolean) {
        val file = pending ?: return
        pending = null
        if (success && file.exists()) {
            photoPath = file.path
        } else {
            unsaved -= file.path
            store.delete(file.path)
        }
    }

    fun remove() {
        photoPath = null
    }

    /** Call after the owning record was saved with [photoPath]. */
    fun commit() {
        if (savedPath != photoPath) store.delete(savedPath)
        unsaved.remove(photoPath)
        discard()
        savedPath = photoPath
    }

    /** Call when the owning record was deleted. */
    fun deleteAll() {
        store.delete(savedPath)
        savedPath = null
        discard()
    }

    /** Drops photos taken during this visit that were never saved. */
    fun discard() {
        unsaved.forEach(store::delete)
        unsaved.clear()
    }
}
