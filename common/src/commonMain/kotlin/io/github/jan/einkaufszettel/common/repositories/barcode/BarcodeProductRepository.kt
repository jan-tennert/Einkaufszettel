package io.github.jan.einkaufszettel.common.repositories.barcode

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

sealed interface BarcodeProductRepository {

    suspend fun getProductByBarCode(barCode: String): BarcodeProduct?

}

class BarcodeProductRepositoryImpl(private val httpClient: HttpClient) : BarcodeProductRepository {

    override suspend fun getProductByBarCode(barCode: String): BarcodeProduct? {
        val data = httpClient.get(BASE_URL.format(barCode)).body<JsonObject>()
        if(data["status"]?.jsonPrimitive?.int == 0) {
            val data2 = httpClient.get(SECOND_URL.format(barCode)).body<JsonObject>()
            if(data2["status"]?.jsonPrimitive?.int == 0) return null
            return BarcodeProduct.fromJson(data2, 1)
        }
        return BarcodeProduct.fromJson(data, 1)
    }

    private companion object {
        const val BASE_URL = "http://de.openfoodfacts.org/api/v2/product/%s"
        const val SECOND_URL = "http://world.openfoodfacts.org/api/v2/product/%s"
    }

}

