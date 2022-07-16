package io.github.jan.einkaufszettel.common.repositories

import android.content.Context
import android.net.Uri
import io.github.jan.einkaufszettel.common.getBytesFromUri
import io.github.jan.einkaufszettel.common.getExtensionFromUri
import io.github.jan.einkaufszettel.common.repositories.card.CardRepository
import io.github.jan.einkaufszettel.common.repositories.card.CardRepositoryImpl
import io.github.jan.supacompose.storage.storage
import kotlinx.datetime.Clock

suspend fun CardRepository.uploadImage(context: Context, uri: Uri): String {
    val extension = context.getExtensionFromUri(uri)
    val bytes = context.getBytesFromUri(uri)!!
    val path = Clock.System.now().toEpochMilliseconds().toString() + ".$extension"
    (this as CardRepositoryImpl).supabaseClient.storage["cards"].upload(path, bytes)
    return path
}