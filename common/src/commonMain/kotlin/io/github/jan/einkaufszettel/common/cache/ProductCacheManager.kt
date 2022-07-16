package io.github.jan.einkaufszettel.common.cache

import io.github.jan.einkaufszettel.common.repositories.product.Shop
import kotlinx.serialization.builtins.ListSerializer

class ProductCacheManager : JsonCacheManager<Shop> {

    override val fileName = "products.json"
    override val serializer = ListSerializer(Shop.serializer())

}