package io.github.jan.einkaufszettel.android

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.material.ScaffoldState
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.Direction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

fun NavController.navigateTo(direction: Direction, scaffoldState: ScaffoldState, scope: CoroutineScope) {
    navigate(direction)
    scope.launch {
        scaffoldState.drawerState.close()
    }
}
