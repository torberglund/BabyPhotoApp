// Utils.kt
package com.example.babyphotoapp

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.*

/**
 * Inserts a new empty image into DCIM/BabyPhotoApp and returns its Uri + ContentValues.
 */
fun createMediaStoreEntry(context: Context): Pair<Uri, ContentValues> {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val displayName = "IMG_$timestamp.jpg"

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        // on Q+ this goes under DCIM/BabyPhotoApp, on older we'll get DATA path
        put(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Images.Media.RELATIVE_PATH
            else
                MediaStore.Images.Media.DATA,
            "DCIM/BabyPhotoApp"
        )
    }

    val resolver = context.contentResolver
    val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val uri = resolver.insert(collection, values)
        ?: throw IllegalStateException("Failed to create MediaStore entry")

    return Pair(uri, values)
}
