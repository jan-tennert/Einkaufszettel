package io.github.jan.einkaufszettel.common.modules

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.Auth
import io.github.jan.supacompose.auth.host
import io.github.jan.supacompose.auth.scheme
import io.github.jan.supacompose.createSupabaseClient
import io.github.jan.supacompose.postgrest.Postgrest
import io.github.jan.supacompose.storage.Storage

actual fun createPlatformSupabaseClient(): SupabaseClient {
    return createSupabaseClient {
        supabaseUrl = "https://arnyfaeuskyqfxkvotgj.supabase.co"
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFybnlmYWV1c2t5cWZ4a3ZvdGdqIiwicm9sZSI6ImFub24iLCJpYXQiOjE2NTMwMzkxMTEsImV4cCI6MTk2ODYxNTExMX0.ItmL8lfnOL9oy7CEX9N6TnYt10VVhk-KTlwley4aq1M"

        install(Auth) {
            scheme = "shoppinglist"
            host = "login"
        }
        install(Postgrest)
        install(Storage)
    }
}