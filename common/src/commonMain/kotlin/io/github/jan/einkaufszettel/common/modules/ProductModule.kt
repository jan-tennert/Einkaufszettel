package io.github.jan.einkaufszettel.common.modules

import io.github.jan.einkaufszettel.common.repositories.card.CardRepository
import io.github.jan.einkaufszettel.common.repositories.card.CardRepositoryImpl
import io.github.jan.einkaufszettel.common.repositories.product.ProductRepository
import io.github.jan.einkaufszettel.common.repositories.product.ProductRepositoryImpl
import io.github.jan.einkaufszettel.common.repositories.shop.ShopRepository
import io.github.jan.einkaufszettel.common.repositories.shop.ShopRepositoryImpl
import org.koin.dsl.module

val productModule = module {
    includes(profileModule)
    includes(cacheModule)
    single<ProductRepository> {
        ProductRepositoryImpl(get())
    }
    single<ShopRepository> {
        ShopRepositoryImpl(get())
    }
    single<CardRepository> {
        CardRepositoryImpl(get())
    }
}
