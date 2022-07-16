package io.github.jan.einkaufszettel.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.jan.einkaufszettel.common.cache.DefaultImageCacheProvider
import io.github.jan.einkaufszettel.common.cache.ImageCacheProvider

@Composable
fun AsyncImage(url: String, modifier: Modifier = Modifier, cacheProvider: ImageCacheProvider = DefaultImageCacheProvider) {
    cacheProvider.Image(url, modifier)
}

@Composable
fun AsyncImage(fileName: String, modifier: Modifier = Modifier, cacheProvider: ImageCacheProvider = DefaultImageCacheProvider, producer: suspend () -> ByteArray) {
    cacheProvider.ImageB(fileName, modifier, producer)
}