package io.github.jan.einkaufszettel.common.modules

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.Auth
import io.github.jan.supacompose.auth.sessionFile
import io.github.jan.supacompose.createSupabaseClient
import io.github.jan.supacompose.postgrest.Postgrest
import io.github.jan.supacompose.storage.Storage
import java.io.File

actual fun createPlatformSupabaseClient(): SupabaseClient {
    val folder = File(System.getProperty("user.home"), "Einkaufszettel")
    if(!folder.exists()) folder.mkdir()
    return createSupabaseClient {
        supabaseUrl = "https://arnyfaeuskyqfxkvotgj.supabase.co"
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFybnlmYWV1c2t5cWZ4a3ZvdGdqIiwicm9sZSI6ImFub24iLCJpYXQiOjE2NTMwMzkxMTEsImV4cCI6MTk2ODYxNTExMX0.ItmL8lfnOL9oy7CEX9N6TnYt10VVhk-KTlwley4aq1M"

        install(Auth) {
            sessionFile = File(folder, "session.json")
        }
        install(Postgrest)
        install(Storage)
    }
}