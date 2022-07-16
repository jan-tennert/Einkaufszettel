package io.github.jan.einkaufszettel.common.repositories.product

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.postgrest.postgrest
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface ProductRepository {

    suspend fun getShops(): List<Shop>

    suspend fun createInShop(shop: Int, content: String, creatorId: String)

    suspend fun markAsDone(productId: Int, userId: String)

    suspend fun markAsNotDone(productId: Int)

    suspend fun editContent(productId: Int, newContent: String)

    suspend fun deleteProduct(id: Int)

    /*suspend fun getAttachments(): List<AttachmentItem>

    suspend fun uploadAttachment(id: String, extension: String, data: String)

    suspend fun deleteAttachment(id: String, extension: String)

    suspend fun getLatestVersion(): Int*/

    companion object {
        const val URL = "http://xowugqynwpspvhno.myfritz.net:26666/api/shopping"
    }

}

class ProductRepositoryImpl(private val supabaseClient: SupabaseClient) : ProductRepository {

    override suspend fun getShops(): List<Shop> = supabaseClient.postgrest["shop_products"].select<Shop>().decodeAs()

    override suspend fun createInShop(shop: Int, content: String, creatorId: String) {
        supabaseClient.postgrest["products"].insert(
            buildJsonObject {
                put("shop_id", shop)
                put("content", content)
                put("user_id", creatorId)
            })
    }

    override suspend fun editContent(productId: Int, newContent: String) {
        supabaseClient.postgrest["products"].update({
            Product::content setTo newContent
        }) {
            Product::id eq productId
        }
    }

    override suspend fun markAsDone(productId: Int, userId: String) {
        supabaseClient.postgrest["products"].update({
            set("done_by", userId)
            Product::doneSince setTo Clock.System.now()
        }) {
            Product::id eq productId
        }
    }

    override suspend fun markAsNotDone(productId: Int) {
        supabaseClient.postgrest["products"].update({
            Product::doneBy setTo null
            Product::doneSince setTo null
        }) {
            Product::id eq productId
        }
    }

    override suspend fun deleteProduct(id: Int) {
        supabaseClient.postgrest["products"].delete {
            Product::id eq id
        }
    }

    /*override suspend fun getAttachments() = httpClient.get("$URL/attachments").body<List<AttachmentItem>>()

    override suspend fun uploadAttachment(id: String, extension: String, data: String) {
        httpClient.post("$URL/attachments") {
            setBody(buildJsonObject {
                put("data", data)
                put("id", id)
                put("extension", extension)
            })
        }
    }

    override suspend fun deleteAttachment(id: String, extension: String) {
        httpClient.delete("$URL/attachments/$id.$extension")
    }

    override suspend fun getLatestVersion() = httpClient.get("$URL/version").body<JsonObject>()["version"]!!.jsonPrimitive.int*/
}