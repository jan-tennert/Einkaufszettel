package io.github.jan.einkaufszettel.common.repositories.shop

import io.github.jan.einkaufszettel.common.repositories.product.Product
import io.github.jan.einkaufszettel.common.repositories.product.Shop
import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.postgrest.postgrest
import io.github.jan.supacompose.storage.storage
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface ShopRepository {

    suspend fun uploadIcon(extension: String, data: ByteArray): String

    suspend fun createShop(
        name: String,
        iconUrl: String,
        owner: String,
        authorizedUsers: List<String>
    )

    suspend fun editShop(
        shopId: Int,
        newName: String,
        authorizedUsers: List<String>
    )

    suspend fun deleteShop(id: Int)

}

class ShopRepositoryImpl(val supabaseClient: SupabaseClient) : ShopRepository {

    override suspend fun createShop(
        name: String,
        iconUrl: String,
        owner: String,
        authorizedUsers: List<String>
    ) {
        supabaseClient.postgrest["shops"].insert(buildJsonObject {
            put("name", name)
            put("icon_url", iconUrl)
            put("owner_id", owner)
            put("authorized_users", JsonArray(authorizedUsers.map { JsonPrimitive(it)}))
        })
    }

    override suspend fun editShop(shopId: Int, newName: String, authorizedUsers: List<String>) {
        supabaseClient.postgrest["shops"].update({
            Shop::name setTo newName
            set("authorized_users", JsonArray(authorizedUsers.map { JsonPrimitive(it)}))
        }) {
            Shop::id eq shopId
        }
    }

    override suspend fun uploadIcon(extension: String, data: ByteArray): String {
        return supabaseClient.storage["icons"].upload(Clock.System.now().toEpochMilliseconds().toString() + ".$extension", data)
    }

    override suspend fun deleteShop(id: Int) {
        supabaseClient.postgrest["products"].delete {
            Product::shopId eq id
        }
        supabaseClient.postgrest["shops"].delete {
            Shop::id eq id
        }
    }

}