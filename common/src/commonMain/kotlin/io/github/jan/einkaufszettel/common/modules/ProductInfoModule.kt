package io.github.jan.einkaufszettel.common.modules

import io.github.jan.einkaufszettel.common.repositories.barcode.BarcodeProductRepository
import io.github.jan.einkaufszettel.common.repositories.barcode.BarcodeProductRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val productInfoModule = module {
    single(qualifier("productInfoClient")) {
        HttpClient(OkHttp) {
            install(DefaultRequest) {
                header("Authorization", "Bearer 086281bd1282f14bbbe661aeb227883decad\$7fe4762e7385581b2145099fe9abb719\$5e38be65d32444a2f15ac0b90783dc1d08c5d45d5eaee88ebb839e89f897250d")
            }
            install(ContentNegotiation) {
                json()
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
                //accept(ContentType.Application.Json)
            }
            BrowserUserAgent()
        }
    }
    single<BarcodeProductRepository> {
        BarcodeProductRepositoryImpl(get(qualifier("productInfoClient")))
    }
}
