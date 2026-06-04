package com.example.data.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import com.example.BuildConfig

object SupabaseApiClient {
    val client: SupabaseClient by lazy {
        val url = if (BuildConfig.SUPABASE_URL.isNotEmpty() && BuildConfig.SUPABASE_URL != "YOUR_SUPABASE_URL") {
            BuildConfig.SUPABASE_URL
        } else {
            "https://xxxx.supabase.co"
        }
        val key = if (BuildConfig.SUPABASE_ANON_KEY.isNotEmpty() && BuildConfig.SUPABASE_ANON_KEY != "YOUR_SUPABASE_ANON_KEY") {
            BuildConfig.SUPABASE_ANON_KEY
        } else {
            "eyJxxxx"
        }

        createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key
        ) {
            install(Auth)
        }
    }
}
