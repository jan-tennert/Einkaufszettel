package io.github.jan.einkaufszettel.common.repositories.shop

import android.content.Context
import android.net.Uri
import io.github.jan.einkaufszettel.common.getBytesFromUri
import io.github.jan.einkaufszettel.common.getExtensionFromUri
import io.github.jan.supacompose.storage.storage
import kotlinx.datetime.Clock

suspend fun ShopRepository.uploadIcon(context: Context, uri: Uri): String {
    val extension = context.getExtensionFromUri(uri)
    val bytes = context.getBytesFromUri(uri)!!
    return (this as ShopRepositoryImpl).supabaseClient.storage["icons"].upload(Clock.System.now().toEpochMilliseconds().toString() + ".$extension", bytes)
}
