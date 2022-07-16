package io.github.jan.einkaufszettel.common.cache

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.io.File
import java.net.URI

actual object DefaultImageCacheProvider : ImageCacheProvider {

    @Composable
    actual override fun ImageB(
        fileName: String,
        modifier: Modifier,
        producer: suspend () -> ByteArray
    ) {
        val cacheDir = File(File(System.getProperty("java.io.tmpdir"), "Einkaufszettel"), "imageCache")
        if(!cacheDir.parentFile.exists()) cacheDir.parentFile.mkdir()
        val image by produceState<ImageBitmap?>(null) {
            if(!cacheDir.exists()) cacheDir.mkdir()
            val uri = URI(fileName)
            val path = uri.path
            val name = path!!.substring(path.lastIndexOf('/') + 1)
            val file = File(cacheDir, name)
            value = if(file.exists()) {
                val data = file.readBytes()
                org.jetbrains.skia.Image.makeFromEncoded(data).toComposeImageBitmap()
            } else {
                try {
                    val data = producer()
                    file.writeBytes(data)
                    org.jetbrains.skia.Image.makeFromEncoded(data).toComposeImageBitmap()
                } catch(e: Exception) {
                  //     e.printStackTrace()
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