package io.github.jan.einkaufszettel.common.repositories.card

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.postgrest.postgrest
import io.github.jan.supacompose.storage.storage
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface CardRepository {

    suspend fun createCard(description: String, authorizedUsers: List<String>, imagePath: String, owner: String)

    suspend fun editCard(id: Int, description: String, authorizedUsers: List<String>)

    suspend fun uploadImage(extension: String, data: ByteArray) : String

    suspend fun getCards() : List<Card>

    suspend fun deleteCard(id: Int, imagePath: String)

}

class CardRepositoryImpl(
    val supabaseClient: SupabaseClient
) : CardRepository {

    override suspend fun createCard(
        description: String,
        authorizedUsers: List<String>,
        imagePath: String,
        owner: String
    ) {
        supabaseClient.postgrest["cards"].insert(buildJsonObject {
            put("description", description)
            put("owner_id", owner)
            put("authorized_users", JsonArray(authorizedUsers.map { JsonPrimitive(it) }))
            put("image_path", imagePath)
        })
    }

    override suspend fun uploadImage(extension: String, data: ByteArray): String {
        val path = Clock.System.now().toEpochMilliseconds().toString() + ".$extension"
        supabaseClient.storage["cards"].upload(path, data)
        return path
    }

    override suspend fun editCard(id: Int, description: String, authorizedUsers: List<String>) {
        supabaseClient.postgrest["cards"].update({
            set("authorized_users", JsonArray(authorizedUsers.map { JsonPrimitive(it) }))
            Card::description setTo description
        }) {
            Card::id eq id
        }
    }

    override suspend fun getCards(): List<Card> = supabaseClient.postgrest["card_view"].select<Card> {  }.decodeAs()

    override suspend fun deleteCard(id: Int, imagePath: String) {
        supabaseClient.postgrest["cards"].delete {
            Card::id eq id
        }
        supabaseClient.storage["cards"].delete(imagePath)
    }

}