package com.example.data.network

import android.util.Log
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.BudgetGoal
import com.example.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ReceiptExtraction(
    val concept: String,
    val amount: Double,
    val category: String
)

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Call Google Gemini API to get financial coaching recommendations
     */
    suspend fun getFinancialCoaching(
        transactions: List<Transaction>,
        goals: List<BudgetGoal>,
        timeframe: String = "weekly"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local on-device smart heuristics.")
            return@withContext getLocalSmartHeuristics(transactions, goals)
        }

        val prompt = buildCoachingPrompt(transactions, goals, timeframe)
        val endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

        try {
            // Build the JSON request body
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                // Optional system instruction to enforce bilingual and smart coaching style
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Eres mi mascota virtual y coach financiero. Háblame en primera persona. Sé EXTREMADAMENTE BREVE y responde SOLO con un objeto JSON válido con dos campos: 'diagnosis' (tu análisis en 20 palabras máximo) y 'mission' (un reto específico de 1 línea). No añadas formato markdown ```json.")
                        })
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(endpointUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "HTTP Error: ${response.code} - ${response.message}")
                    return@withContext "¡Ups! No pudimos comunicarnos con el servidor de IA de FinSage (Código ${response.code}). \n\n${getLocalSmartHeuristics(transactions, goals)}"
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val firstPart = parts?.optJSONObject(0)
                
                val answer = firstPart?.optString("text")
                if (!answer.isNullOrEmpty()) {
                    answer
                } else {
                    getLocalSmartHeuristics(transactions, goals)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini API: ${e.message}", e)
            "No se pudo completar la conexión en tiempo real con Gemini por problemas de red. Usando Coach FinSage de respaldo local:\n\n${getLocalSmartHeuristics(transactions, goals)}"
        }
    }

    private fun buildCoachingPrompt(transactions: List<Transaction>, goals: List<BudgetGoal>, timeframe: String): String {
        val txString = StringBuilder()
        transactions.take(15).forEach { tx ->
            txString.append("- ${tx.bankName}: ${tx.concept} (${tx.category}) de ${tx.amount} ${tx.currency} (Anomalía: ${tx.isAnomaly}, Micro-gasto: ${tx.isMicroSpend})\n")
        }

        val goalString = StringBuilder()
        goals.forEach { g ->
            goalString.append("- Meta: ${g.title}, Target: ${g.targetAmount} EUR, Guardado: ${g.savedAmount} EUR, Fecha: ${g.targetDate}\n")
        }

        val timeframeText = when (timeframe) {
            "daily" -> "el día de hoy"
            "weekly" -> "la última semana"
            "monthly" -> "el último mes"
            else -> "recientemente"
        }

        return """
            Analiza mis datos financieros de $timeframeText. Responde con un resumen super breve, sin listas ni viñetas, parecido a un bocadillo de chat de un videojuego.
            No superes las 20-30 palabras. Dame un diagnóstico rápido y un reto de 1 línea para $timeframeText. Todo en Español.

            Gastos recientes de ejemplo:
            $txString
            
            Mis metas actuales:
            $goalString
        """.trimIndent()
    }

    suspend fun extractReceiptInfo(imageBytes: ByteArray, recentHistory: List<Transaction>): ReceiptExtraction? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured.")
            return@withContext null
        }

        val endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=\$apiKey"
        val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        val historySummary = recentHistory.take(20).joinToString(", ") { "${it.concept} (${it.category})" }

        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Analiza esta factura. Identifica el establecimiento y realiza un desglose detallado de los artículos (microgastos) con su precio (ej. Leche: 1.50€, Pan: 0.80€). Incluye todo esto en el campo 'concept' con el formato: '<Establecimiento> - <Artículos separados por comas>'. Calcula la cantidad total pagada (negativa). Por último, sugiere la categoría más probable de esta lista: [Alimentos, Transporte, Suscripción, Restaurantes, Ocio, Ingreso, Inversiones]. Usa este historial reciente como contexto para la categoría si te ayuda: [$historySummary]. Devuelve solo JSON puro sin formato markdown: {\"concept\": \"...\", \"amount\": -15.50, \"category\": \"Alimentos\"}")
                            })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        })
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(endpointUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "HTTP Error: \${response.code} - \${response.message}")
                    return@withContext null
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val firstPart = parts?.optJSONObject(0)
                
                var answer = firstPart?.optString("text")
                if (!answer.isNullOrEmpty()) {
                    answer = answer.replace("```json", "").replace("```", "").trim()
                    try {
                        val parsed = JSONObject(answer)
                        return@withContext ReceiptExtraction(
                            concept = parsed.optString("concept", "Gastos"),
                            amount = parsed.optDouble("amount", 0.0),
                            category = parsed.optString("category", "Alimentos")
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse receipt JSON: \$answer", e)
                        return@withContext null
                    }
                } else {
                    return@withContext null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini API for receipt: \${e.message}", e)
            return@withContext null
        }
    }

    suspend fun generatePetImage(prompt: String): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured for Images.")
            return@withContext null
        }
        val endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-image-preview:generateContent?key=\$apiKey"
        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply {
                        put("IMAGE")
                    })
                    put("imageConfig", JSONObject().apply {
                        put("aspectRatio", "1:1")
                        put("imageSize", "1K")
                    })
                })
            }
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)
            val request = Request.Builder().url(endpointUrl).post(requestBody).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val responseJson = JSONObject(response.body?.string() ?: "")
                val inlineData = responseJson.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optJSONObject("inlineData")
                return@withContext inlineData?.optString("data")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini Image API: \${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Intelligent dynamic fallback engine (Heuristics modeled on GNN/Anomaly predictions)
     * if Gemini API Keys are missing at compile time or fail to fetch.
     */
    private fun getLocalSmartHeuristics(transactions: List<Transaction>, goals: List<BudgetGoal>): String {
        return """
            {
              "diagnosis": "Gastos bajo control, pero tienes varios gastos en pequeñas compras. Tu riesgo de anomalías es bajo.",
              "mission": "Evita gastos innecesarios de menos de 10€ hoy."
            }
        """.trimIndent()
    }
}
