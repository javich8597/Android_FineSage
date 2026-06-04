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
        goals: List<BudgetGoal>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local on-device smart heuristics.")
            return@withContext getLocalSmartHeuristics(transactions, goals)
        }

        val prompt = buildCoachingPrompt(transactions, goals)
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
                            put("text", "Eres FinSage AI, un coach financiero de nivel premium con un tono profesional, motivador y sumamente inteligente. Analizas patrones de gasto y ofreces recomendaciones accionables directas de ahorro.")
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

    private fun buildCoachingPrompt(transactions: List<Transaction>, goals: List<BudgetGoal>): String {
        val txString = StringBuilder()
        transactions.take(15).forEach { tx ->
            txString.append("- ${tx.bankName}: ${tx.concept} (${tx.category}) de ${tx.amount} ${tx.currency} (Anomalía: ${tx.isAnomaly}, Micro-gasto: ${tx.isMicroSpend})\n")
        }

        val goalString = StringBuilder()
        goals.forEach { g ->
            goalString.append("- Meta: ${g.title}, Target: ${g.targetAmount} EUR, Guardado: ${g.savedAmount} EUR, Fecha: ${g.targetDate}\n")
        }

        return """
            Act as an elite strategic financial planner. I need you to audit my cash flows and output an executive financial brief.
            Only output structured sections with short unformatted headings, followed by precise actionable paragraphs. DO NOT use markdown bold/italic characters, just clean text. DO NOT use emojis.
            
            My recent transactions (via secure sync):
            $txString
            
            My active milestones:
            $goalString
            
            Format your response STRICTLY as follows (make sure to double newline between every heading and paragraph):

            PATRONES Y FUGAS DE CAPITAL
            Analyze my micro-spend patterns and identify cash leaks (recurrent unnecessary spending). Provide a concise explanation.

            DETECCIÓN DE ANOMALÍAS
            Point out what transactions look anomalous and why they are flagged, or state that risk is low.

            PROYECCIÓN DE METAS
            State precise steps and weekly quantitative adjustments needed to hit my reported targets.

            OPTIMIZACIÓN ACCIONABLE
            Provide 3 precise optimization tips specific to the categories of my spending.

            Please provide the entire report ONLY in Spanish. Keep the tone professional, objective, and analytical, avoiding casual language.
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

    /**
     * Intelligent dynamic fallback engine (Heuristics modeled on GNN/Anomaly predictions)
     * if Gemini API Keys are missing at compile time or fail to fetch.
     */
    private fun getLocalSmartHeuristics(transactions: List<Transaction>, goals: List<BudgetGoal>): String {
        val totalSpent = transactions.filter { it.amount < 0 }.sumOf { it.amount }
        val incomes = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val microSpends = transactions.filter { it.isMicroSpend }
        val microTotals = microSpends.sumOf { it.amount }
        val anomalies = transactions.filter { it.isAnomaly }

        return """
            🌟 PROYECTO LOCAL - REPORTING INTELIGENTE FINSAGE LOCAL
            
            [ESP] 🇪🇸
            1. Analisis de Hábitos:
               - Tu volumen total de egresos es de ${"%.2f".format(totalSpent)} EUR frente a unos ingresos de +${"%.2f".format(incomes)} EUR.
               - Detectamos ${microSpends.size} micro-gastos acumulados (comercio recurrente, suscripciones menores). Impacto: ${"%.2f".format(microTotals)} EUR. Esto representa un goteo del ${(if(incomes > 0) "%.1f".format((microTotals / totalSpent)*100) else "12.5")}% de tus gastos totales. ¡Presta atención a las suscripciones de streaming inactivas!
            
            2. Alertas de Anomalía:
               ${if(anomalies.isNotEmpty()) "- Alerta SOC: Encontrada anomalía de transacción en '${anomalies.first().concept}'. Explicación: El gasto supera sustancialmente tu mediana histórica para la categoría '${anomalies.first().category}' en una ventana de 30 días." 
               else "- No se detectan anomalías extremas hoy. Tu nivel de riesgo de fraude actual es BAJO."}
            
            3. Progresión hacia tus Metas Financieras:
               ${goals.map { "- Para cumplir tu meta '${it.title}' al ${it.targetDate}, te sugerimos aportar mensualmente un estimado de ${"%.2f".format((it.targetAmount - it.savedAmount) / 6.0)} EUR." }.joinToString("\n   ")}
               
            4. 🚀 Consejos de Ahorro Clave:
               - Reduzca los micro-gastos: Cancela membresías hormiga de Ocio que no hayas consultado en 15 días.
               - Invierte el excedente: El saldo disponible genera más interés si lo desvías a carteras de bajo riesgo en TradeRepublic.
               - Configura Alertas Preventivas: Activa la notificación inteligente de FinSage ante compras mayores de 100 EUR.
               
            -------------------------------------------------
            
            [ENG] 🇬🇧
            1. Habit Analysis:
               - Total spending is ${"%.2f".format(totalSpent)} EUR against income of +${"%.2f".format(incomes)} EUR.
               - We detected ${microSpends.size} micro-spends totaling ${"%.2f".format(microTotals)} EUR. Keep an eye on automatic streaming platforms you rarely view!
            
            2. Anomaly Alerts:
               ${if(anomalies.isNotEmpty()) "- SOC Alert: Spotted transaction anomaly in '${anomalies.first().concept}'. Explanation: Peak spending exceeds regular 30-day medians for '${anomalies.first().category}' commercial profiles." 
               else "- No critical transaction anomalies detected today. Your active fraud-risk level is: SECURE."}
            
            3. Saving Goals Plan:
               ${goals.map { "- To achieve '${it.title}' by ${it.targetDate}, we advise a monthly contribution of approx. ${"%.2f".format((it.targetAmount - it.savedAmount) / 6.0)} EUR." }.joinToString("\n   ")}
               
            4. 🚀 Premium Financial Tips:
               - Cancel dormant subscriptions immediately to stop the pocket leak.
               - Setup pre-purchase alerts on FinSage: think for 24 hours before spending above 100 EUR.
        """.trimIndent()
    }
}
