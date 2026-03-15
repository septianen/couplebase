package com.couplebase.core.network

/**
 * Supabase configuration holder.
 * Values should be provided via platform-specific config (BuildConfig, plist, env).
 */
data class SupabaseConfig(
    val url: String,
    val anonKey: String,
)
