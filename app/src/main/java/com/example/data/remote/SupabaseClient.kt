package com.example.data.remote

import com.example.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

object SupabaseClient {
    private const val TAG = "SupabaseClient"
    
    // Note: To use these keys, the user MUST configure them via AI Studio Secrets Panel
    // If not configured, they default to empty string from BuildConfig.
    private val SUPABASE_URL: String = BuildConfig.SUPABASE_URL ?: ""
    private val SUPABASE_ANON_KEY: String = BuildConfig.SUPABASE_ANON_KEY ?: ""

    private val client = HttpClient(Android) {
        engine {
            connectTimeout = 10_000
            socketTimeout = 10_000
        }
    }

    suspend fun testConnection(): Boolean {
        if (SUPABASE_URL.isEmpty() || SUPABASE_ANON_KEY.isEmpty() || SUPABASE_URL.contains("your-supabase-url")) {
            Log.e(TAG, "Supabase Credentials not configured. Please use the Secrets panel.")
            return false
        }
        return try {
            withContext(Dispatchers.IO) {
                // Testing connection by trying to reach the root REST endpoint
                val response: HttpResponse = client.get("$SUPABASE_URL/rest/v1/") {
                    header("apikey", SUPABASE_ANON_KEY)
                    header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                }
                response.status.isSuccess()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase connection failed: \${e.message}")
            false
        }
    }
    
    suspend fun getActiveMissions(): String? {
        if (SUPABASE_URL.isEmpty() || SUPABASE_ANON_KEY.isEmpty() || SUPABASE_URL.contains("your-supabase-url")) {
            return null
        }
        return try {
            withContext(Dispatchers.IO) {
                // Fetch from a 'missions' table. You should create this table in Supabase!
                val response: HttpResponse = client.get("$SUPABASE_URL/rest/v1/missions?select=*&status=eq.active") {
                    header("apikey", SUPABASE_ANON_KEY)
                    header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                    header("Range", "0-9") 
                }
                if (response.status.isSuccess()) {
                    response.bodyAsText()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching missions: \${e.message}")
            null
        }
    }
}
