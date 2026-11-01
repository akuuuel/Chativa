package com.chativa.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.Json

object SupabaseConfig {
    const val DEFAULT_SUPABASE_URL: String = "https://ltywlisxoeaqhryffdxe.supabase.co"
    const val DEFAULT_SUPABASE_ANON_KEY: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx0eXdsaXN4b2VhcWhyeWZmZHhlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTM0OTM1NjgsImV4cCI6MjEwOTA2OTU2OH0.wZXUog-Vm2K6tIX2eacBBaYivmRhcemmo84YaCJBuwY"

    var SUPABASE_URL: String = DEFAULT_SUPABASE_URL
    var SUPABASE_ANON_KEY: String = DEFAULT_SUPABASE_ANON_KEY

    @Volatile
    private var _client: SupabaseClient? = null

    val client: SupabaseClient
        get() {
            return _client ?: synchronized(this) {
                _client ?: createSupabaseClient(
                    supabaseUrl = SUPABASE_URL,
                    supabaseKey = SUPABASE_ANON_KEY
                ) {
                    defaultSerializer = KotlinXSerializer(
                        Json {
                            ignoreUnknownKeys = true
                            coerceInputValues = true
                            isLenient = true
                            encodeDefaults = true
                        }
                    )
                    install(Auth)
                    install(Postgrest)
                    install(Realtime)
                    install(Storage)
                }.also { _client = it }
            }
        }

    fun updateConfig(url: String, anonKey: String) {
        SUPABASE_URL = url.trim()
        SUPABASE_ANON_KEY = anonKey.trim()
        _client = null
    }

    val auth get() = client.auth
    val postgrest get() = client.postgrest
    val realtime get() = client.realtime
    val storage get() = client.storage
}
