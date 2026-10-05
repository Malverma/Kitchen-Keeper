package com.kitchenkeeper.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/** Creates and deletes item photos in app-private storage (filesDir/images). */
class PhotoStore(private val context: Context) {
    private val imagesDir: File
        get() = File(context.filesDir, "images").apply { mkdirs() }

    fun newPhotoFile(): File = File(imagesDir, "${UUID.randomUUID()}.jpg")

    fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** Copies the photo at [path] to a new file and returns its path, or null if there is none. */
    fun copy(path: String?): String? {
        val source = path?.let(::File)?.takeIf { it.exists() } ?: return null
        return source.copyTo(newPhotoFile()).path
    }

    fun delete(path: String?) {
        if (path != null) File(path).delete()
    }
}
