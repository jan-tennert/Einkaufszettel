@file:JvmName("UtilsAndroid")
package io.github.jan.einkaufszettel.common

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

fun Context.getExtensionFromUri(uri: Uri): String {
    var path = ""
    if (contentResolver != null) {
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
            path = cursor.getString(idx)
            cursor.close()
        }
    }
    return path.split(".").last()
}

fun Context.getBytesFromUri(uri: Uri) = contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() }