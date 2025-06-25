package com.example.babyphotoapp

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun createFile(context: Context): File {
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    val path = sdf.format(Date())
    val dir = File(context.filesDir, "photos/$path")
    if (!dir.exists()) dir.mkdirs()
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return File(dir, "${timestamp}.jpg")
}
