package io.github.jan.einkaufszettel.common.cache

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import java.net.URI

internal val HTTP_CLIENT = HttpClient(OkHttp)

interface ImageCacheProvider {

    @Composable
    fun Image(url: String, modifier: Modifier, request: HttpRequestBuilder.() -> Unit = {}) {
        val uri = URI(url)
        val path: String = uri.path
        val fileName = path.substring(path.lastIndexOf('/') + 1)
        ImageB(fileName, modifier) {
            HTTP_CLIENT.request(url) {
                method = HttpMethod.Get
                request()
            }.body()
        }
    }

    @Composable
    fun ImageB(fileName: String, modifier: Modifier, producer: suspend () -> ByteArray)

}

expect object DefaultImageCacheProvider : ImageCacheProvider {

    @Composable
    override fun ImageB(fileName: String, modifier: Modifier, producer: suspend () -> ByteArray)

}