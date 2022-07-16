package io.github.jan.einkaufszettel.common.cache

import android.content.Context

suspend fun <T> CacheManager<T>.loadCache(context: Context): T = loadCache(context.cacheDir)

suspend fun <T> CacheManager<T>.saveCache(cache: T, context: Context) = saveCache(cache, context.cacheDir)