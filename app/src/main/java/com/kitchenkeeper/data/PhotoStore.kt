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

    fun delete(path: String?) {
        if (path != null) File(path).delete()
    }
}
