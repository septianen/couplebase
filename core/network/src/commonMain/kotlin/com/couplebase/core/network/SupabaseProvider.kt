package com.couplebase.core.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {

    private var client: SupabaseClient? = null

    fun initialize(supabaseUrl: String, supabaseKey: String): SupabaseClient {
        return client ?: createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }.also { client = it }
    }

    fun getClient(): SupabaseClient {
        return client ?: error("SupabaseProvider not initialized. Call initialize() first.")
    }
}
