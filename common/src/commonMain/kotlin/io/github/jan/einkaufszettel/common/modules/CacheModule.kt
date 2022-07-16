package io.github.jan.einkaufszettel.common.modules

import io.github.jan.einkaufszettel.common.cache.CardCacheManager
import io.github.jan.einkaufszettel.common.cache.ProductCacheManager
import org.koin.dsl.module

val cacheModule = module {

    single { CardCacheManager() }
    single { ProductCacheManager() }

}