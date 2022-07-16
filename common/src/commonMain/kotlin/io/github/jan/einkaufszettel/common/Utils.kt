package io.github.jan.einkaufszettel.common

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import java.io.File

val EMAIL_REGEX =
    "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])".toRegex()

suspend fun HttpClient.downloadFile(file: File, url: String, callback: (Float) -> Unit) {
    val response = get(url)
    var offset = 0
    val byteBufferSize = 1024 * 100
    val channel = response.body<ByteReadChannel>()
    val contentLen = response.contentLength()?.toInt() ?: 0
    val data = ByteArray(contentLen)
    do {
        val currentRead = channel.readAvailable(data, offset, byteBufferSize)
        callback((if(contentLen == 0) 0 else ( offset / contentLen.toDouble() ) * 100).toFloat())
        offset += currentRead
    } while (currentRead >= 0)
    file.writeBytes(data)
}