package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.remote.SupabaseApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.financeDao()

        val unsynced = dao.getUnsyncedTransactions()
        if (unsynced.isEmpty()) {
            return Result.success()
        }

        try {
            val retrofit = createRetrofit()
            val supabaseApi = retrofit.create(SupabaseApi::class.java)

            // Make sure these are set in your local.properties or environment!
            // Example fallback logic for safety during tests
            val apiUrl = BuildConfig.SUPABASE_URL
            val apiKey = BuildConfig.SUPABASE_ANON_KEY

            if (apiUrl.isEmpty() || apiKey.isEmpty()) {
                Log.e("SyncWorker", "Missing Supabase credentials in BuildConfig")
                return Result.retry() 
            }

            val response = supabaseApi.syncTransactions(
                apiKey = apiKey,
                authHeader = "Bearer $apiKey",
                transactions = unsynced
            )

            if (response.isSuccessful) {
                // Mark as synced locally
                dao.markTransactionsAsSynced(unsynced.map { it.id })
                Log.i("SyncWorker", "Successfully synced ${unsynced.size} transactions to Supabase")
                return Result.success()
            } else {
                Log.e("SyncWorker", "Failed to sync: ${response.code()} ${response.message()}")
                return Result.retry()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Exception during sync", e)
            return Result.retry()
        }
    }

    private fun createRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val baseUrl = BuildConfig.SUPABASE_URL.let { 
             if (it.isNotEmpty() && !it.endsWith("/")) "$it/" else it
        }.ifEmpty { "https://example.supabase.co/" } // Fallback to avoid crash

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
