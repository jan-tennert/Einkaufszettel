package io.github.jan.einkaufszettel.common.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File

sealed interface CacheManager <T> {

    val fileName: String

    suspend fun loadCache(cacheDir: File): T

    suspend fun saveCache(cache: T, cacheDir: File)

}

sealed interface JsonCacheManager <T : Any> : CacheManager <List<T>> {

    override val fileName: String
    val serializer: KSerializer<List<T>>

    override suspend fun loadCache(cacheDir: File): List<T> {
        if(!cacheDir.parentFile.exists()) cacheDir.parentFile.mkdir()
        val file = File(cacheDir, fileName)
        return if(!file.exists()) {
            listOf()
        } else {
            withContext(Dispatchers.IO) {
                val data = file.readText()
                try {
                    Json.decodeFromString(serializer, data)
                } catch(e: Exception) {
                    e.printStackTrace()
                    listOf()
                }
            }
        }
    }

    override suspend fun saveCache(cache: List<T>, cacheDir: File) {
        if(!cacheDir.parentFile.exists()) cacheDir.parentFile.mkdir()
        val file = File(cacheDir, fileName)
        withContext(Dispatchers.IO) {
            if(!file.exists()) file.createNewFile()
            file.writeText(Json.encodeToString(serializer, cache))
        }
    }

}