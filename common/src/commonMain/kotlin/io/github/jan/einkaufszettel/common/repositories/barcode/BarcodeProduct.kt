package io.github.jan.einkaufszettel.common.repositories.barcode

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class Allergen(private val value: String) {
    GLUTEN("gluten"),
    NUTS("nuts"),
    MILK("milk");

    companion object {

        fun fromValue(value: String): Allergen? {
            return values().firstOrNull { it.value == value }
        }

    }
}

@Serializable
data class BarcodeProduct(val barCode: String, val name: String, val imageUrl: String?, val allergens: Set<Allergen>?, val allergenInfo: List<String>) {

    companion object {

        fun fromJson(data: JsonObject, type: Int): BarcodeProduct = when(type) {
            1 -> {
                val product = data["product"] as JsonObject
                val barCode = data["code"]!!.jsonPrimitive.content
                val name = product["product_name"]!!.jsonPrimitive.content
                val imageUrl = product["image_url"]?.jsonPrimitive?.content
                val allergens = product["allergens"]?.jsonPrimitive?.content?.split(",")
                    ?.mapNotNull { Allergen.fromValue(it.replace("en:", "")) }
                    ?.toSet()
                val allergenInfo = product["allergens_from_ingredients"]?.jsonPrimitive?.content?.split(",")?.drop(1) ?: emptyList()
                BarcodeProduct(barCode, name, imageUrl, allergens, allergenInfo)
            }
            3 -> {
                val product = data["data"] as JsonObject
                val barcode = product["barcode"]!!.jsonPrimitive.content
                val name = product["name"]!!.jsonPrimitive.content
                val imageUrl = product["image_url"]?.jsonPrimitive?.content
                BarcodeProduct(barcode, name, imageUrl, null, emptyList())
            }
            else -> TODO()
        }

    }

}
