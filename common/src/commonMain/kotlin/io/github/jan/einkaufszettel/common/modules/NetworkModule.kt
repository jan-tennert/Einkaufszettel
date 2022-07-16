package io.github.jan.einkaufszettel.common.modules

import io.github.jan.supacompose.SupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import org.koin.dsl.module

val networkModule = module {
    single {
        HttpClient(OkHttp) {
            install(DefaultRequest) {
                header("apiKey", "twQVeR9fKkytbuBTFqGxdzWYrNwA2gtDNXtWQmPzDJs")
            }
            install(ContentNegotiation) {
                json()
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
            BrowserUserAgent()
        }
    }
    single {
        createPlatformSupabaseClient()
    }
}

expect fun createPlatformSupabaseClient(): SupabaseClient