// File: app/src/main/java/com/example/babyphotoapp/Utils.kt
package com.example.babyphotoapp

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.*

/**
 * Inserts a new empty image into DCIM/BabyPhotoApp/yyyy/MM/dd/ and returns its Uri + ContentValues.
 */
fun createMediaStoreEntry(context: Context): Pair<Uri, ContentValues> {
    val now = Date()
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now)
    val displayName = "IMG_$timestamp.jpg"
    val datePath = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(now)

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // now with per-day subfolder
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "DCIM/BabyPhotoApp/$datePath/"
            )
        } else {
            put(MediaStore.Images.Media.DATA, "DCIM/BabyPhotoApp/$datePath/$displayName")
        }
    }

    val resolver = context.contentResolver
    val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val uri = resolver.insert(collection, values)
        ?: throw IllegalStateException("Failed to create MediaStore entry")

    return uri to values
}
