package io.github.jan.einkaufszettel.common.cache

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.File

actual object DefaultImageCacheProvider : ImageCacheProvider {

    @Composable
    actual override fun ImageB(
        fileName: String,
        modifier: Modifier,
        producer: suspend () -> ByteArray
    ) {
        val context = LocalContext.current
        val cacheDir = File(context.cacheDir, "imageCache")
        val image by produceState<ImageBitmap?>(null) {
            if(!cacheDir.exists()) cacheDir.mkdir()
            val uri = Uri.parse(fileName)
            val path = uri.path
            val name = path!!.substring(path.lastIndexOf('/') + 1)
            val file = File(cacheDir, name)
            value = if(file.exists()) {
                val data = file.readBytes()
                BitmapFactory.decodeByteArray(data, 0, data.size).asImageBitmap()
            } else {
                try {
                    val data = producer()
                    file.writeBytes(data)
                    BitmapFactory.decodeByteArray(data, 0, data.size).asImageBitmap()
                } catch(e: Exception) {
                    //   e.printStackTrace()
                    null
                }
            }
        }
        if(image != null) {
            androidx.compose.foundation.Image(image!!, "", modifier)
        } else {
            CircularProgressIndicator()
        }
    }

}