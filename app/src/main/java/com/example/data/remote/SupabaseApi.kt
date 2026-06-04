package com.example.data.remote

import com.example.data.model.Transaction
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SupabaseApi {
    @POST("rest/v1/transactions")
    suspend fun syncTransactions(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body transactions: List<Transaction>
    ): Response<Unit>
}
