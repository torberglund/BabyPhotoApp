// File: app/src/main/java/com/example/babyphotoapp/data/PhotoStore.kt
package com.example.babyphotoapp.data

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object PhotoStore {
    /** Query MediaStore for all URIs in DCIM/BabyPhotoApp for today */
    fun listTodayUris(context: Context): List<Uri> {
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val todayPath = "DCIM/BabyPhotoApp/${java.text.SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())}/"
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} = ?"
        val args      = arrayOf(todayPath)
        val sort       = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        return context.contentResolver.query(collection, projection, selection, args, sort)
            ?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                buildList {
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        add(Uri.withAppendedPath(collection, id.toString()))
                    }
                }
            } ?: emptyList()
    }

    /** Given a MediaStore Uri, look up its DISPLAY_NAME (filename) */
    fun fileNameFromUri(context: Context, uri: Uri): String {
        val proj = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        context.contentResolver.query(uri, proj, null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndexOrThrow(proj[0])
                    return cursor.getString(idx)
                }
            }
        return uri.lastPathSegment ?: ""
    }

    /** A simple device-ID to tag your shots */
    fun deviceId(context: Context): String =
        android.os.Build.MODEL.lowercase()
}
