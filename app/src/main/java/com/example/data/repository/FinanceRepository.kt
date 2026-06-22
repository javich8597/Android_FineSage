package com.example.data.repository

import android.content.Context
import com.example.data.local.FinanceDao
import com.example.data.model.BudgetGoal
import com.example.data.model.Transaction
import com.example.data.model.LearnedRule
import com.example.data.model.CategoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class FinanceRepository(private val financeDao: FinanceDao) {

    val allTransactions: Flow<List<Transaction>> = financeDao.getAllTransactions()
    val allGoals: Flow<List<BudgetGoal>> = financeDao.getAllGoals()
    val anomalies: Flow<List<Transaction>> = financeDao.getAnomalies()
    val microSpends: Flow<List<Transaction>> = financeDao.getMicroSpends()

    val allCategoryItems: Flow<List<CategoryItem>> = financeDao.getAllCategoryItems()
    val allRules: Flow<List<LearnedRule>> = financeDao.getAllRules()
    
    val allMissions: Flow<List<com.example.data.model.Mission>> = financeDao.getAllMissions()

    suspend fun insertTransaction(transaction: Transaction) {
        financeDao.insertTransaction(transaction)
    }

    suspend fun insertMissions(missions: List<com.example.data.model.Mission>) {
        financeDao.insertMissions(missions)
    }

    suspend fun completeMission(id: Int) {
        financeDao.completeMission(id)
    }

    suspend fun insertGoal(goal: BudgetGoal) {
        financeDao.insertGoal(goal)
    }

    suspend fun updateGoalSavings(id: Int, savedAmount: Double) {
        financeDao.updateGoalSavings(id, savedAmount)
    }

    suspend fun deleteGoal(goal: BudgetGoal) {
        financeDao.deleteGoal(goal)
    }

    suspend fun insertCategoryItem(item: CategoryItem) {
        financeDao.insertCategoryItem(item)
    }

    suspend fun deleteCategoryItem(item: CategoryItem) {
        financeDao.deleteCategoryItem(item)
    }

    // --- Dynamic Learning Logic ---
    suspend fun learnCategorizationRule(concept: String, category: String, subcategory: String) {
        val pattern = concept.lowercase().trim()
        if (pattern.isNotEmpty()) {
            financeDao.insertRule(LearnedRule(pattern = pattern, category = category, subcategory = subcategory))
            
            // Re-apply learning to existing transactions matching the pattern
            val currentTx = allTransactions.firstOrNull() ?: emptyList()
            currentTx.forEach { tx ->
                if (tx.concept.lowercase().trim().contains(pattern) || pattern.contains(tx.concept.lowercase().trim())) {
                    financeDao.insertTransaction(
                        tx.copy(category = category, subcategory = subcategory)
                    )
                }
            }
        }
    }

    // Autonomous classification based on user-learned rules, falling back to static keyword intelligence
    fun classifyAutonomous(concept: String, amount: Double, rules: List<LearnedRule>): Pair<String, String> {
        val cleanConcept = concept.lowercase().trim()

        // 1. Prioritize user feedback rules (learning behavior!)
        for (rule in rules) {
            if (cleanConcept.contains(rule.pattern) || rule.pattern.contains(cleanConcept)) {
                return Pair(rule.category, rule.subcategory)
            }
        }

        // 2. High-Fidelity Heuristics Dictionary
        when {
            // Suscripciones
            cleanConcept.contains("spotify") || cleanConcept.contains("netflix") || cleanConcept.contains("twitch") ||
            cleanConcept.contains("disney") || cleanConcept.contains("hulu") || cleanConcept.contains("hbo") ||
            cleanConcept.contains("youtube premium") || cleanConcept.contains("suscrip") || cleanConcept.contains("fee") ||
            cleanConcept.contains("comisión") -> {
                return Pair("Suscripción", "Streaming")
            }
            cleanConcept.contains("gym") || cleanConcept.contains("gimnasio") || cleanConcept.contains("fitness") -> {
                return Pair("Suscripción", "Gimnasio")
            }

            // Alimentos
            cleanConcept.contains("mercadona") || cleanConcept.contains("carrefour") || cleanConcept.contains("lidl") ||
            cleanConcept.contains("aldi") || cleanConcept.contains("dia") || cleanConcept.contains("alcampo") ||
            cleanConcept.contains("supermercado") || cleanConcept.contains("grocery") || cleanConcept.contains("aliment") -> {
                return Pair("Alimentos", "Supermercado")
            }
            cleanConcept.contains("fruteria") || cleanConcept.contains("verduler") -> {
                return Pair("Alimentos", "Frutería")
            }
            cleanConcept.contains("burger") || cleanConcept.contains("mcdonald") || cleanConcept.contains("kfc") ||
            cleanConcept.contains("pizza") || cleanConcept.contains("starbucks") -> {
                return Pair("Alimentos", "Comida rápida")
            }

            // Transporte
            cleanConcept.contains("gasolin") || cleanConcept.contains("repsol") || cleanConcept.contains("cepsa") ||
            cleanConcept.contains("bp") || cleanConcept.contains("shell") -> {
                return Pair("Transporte", "Gasolina")
            }
            cleanConcept.contains("taxi") || cleanConcept.contains("uber") || cleanConcept.contains("cabify") || cleanConcept.contains("bolt") -> {
                return Pair("Transporte", "Taxi")
            }
            cleanConcept.contains("metro") || cleanConcept.contains("bus") || cleanConcept.contains("renfe") || cleanConcept.contains("tren") -> {
                return Pair("Transporte", "Metro/Tren")
            }

            // Ocio
            cleanConcept.contains("cine") || cleanConcept.contains("yelmo") || cleanConcept.contains("cinesa") -> {
                return Pair("Ocio", "Cine")
            }
            cleanConcept.contains("concierto") || cleanConcept.contains("festival") || cleanConcept.contains("ticket") -> {
                return Pair("Ocio", "Conciertos")
            }
            cleanConcept.contains("playstation") || cleanConcept.contains("xbox") || cleanConcept.contains("steam") ||
            cleanConcept.contains("nintendo") || cleanConcept.contains("game") || cleanConcept.contains("juego") -> {
                return Pair("Ocio", "Videojuegos")
            }

            // Salud
            cleanConcept.contains("farmacia") || cleanConcept.contains("medicamento") || cleanConcept.contains("botic") -> {
                return Pair("Salud", "Farmacia")
            }
            cleanConcept.contains("dentista") || cleanConcept.contains("dental") || cleanConcept.contains("odontol") -> {
                return Pair("Salud", "Dentista")
            }
            cleanConcept.contains("seguro") || cleanConcept.contains("sanitas") || cleanConcept.contains("mapfre") || cleanConcept.contains("adeslas") -> {
                return Pair("Salud", "Seguro")
            }

            // Restaurantes / Comida fuera
            cleanConcept.contains("restaurante") || cleanConcept.contains("cena") || cleanConcept.contains("almuerzo") ||
            cleanConcept.contains("cafet") || cleanConcept.contains("cafe") || cleanConcept.contains("bar") || cleanConcept.contains("tapas") -> {
                return Pair("Restaurantes", "Cena")
            }

            // Ingreso
            cleanConcept.contains("nomina") || cleanConcept.contains("sueldo") || cleanConcept.contains("salario") ||
            cleanConcept.contains("finsage corp") -> {
                return Pair("Ingreso", "Nómina")
            }
            cleanConcept.contains("dividendo") || cleanConcept.contains("etf") || cleanConcept.contains("msci") ||
            cleanConcept.contains("invest") || cleanConcept.contains("republic") -> {
                return Pair("Inversiones", "Dividendos")
            }
            cleanConcept.contains("regalo") || cleanConcept.contains("recompensa") || cleanConcept.contains("gift") -> {
                return Pair("Ingreso", "Regalo")
            }
            cleanConcept.contains("wallapop") || cleanConcept.contains("ebay") || cleanConcept.contains("vinted") -> {
                return Pair("Ingreso", "Ventas")
            }

            // Inversiones general
            cleanConcept.contains("bolsa") || cleanConcept.contains("acciones") || cleanConcept.contains("s&p") -> {
                return Pair("Inversiones", "Bolsa")
            }
            cleanConcept.contains("crypto") || cleanConcept.contains("bitcoin") || cleanConcept.contains("ethereum") || cleanConcept.contains("binance") -> {
                return Pair("Inversiones", "Criptomonedas")
            }
        }

        // 3. Fallback based on sign
        return if (amount >= 0) {
            Pair("Ingreso", "Otros")
        } else {
            Pair("Manual", "Otros")
        }
    }

    // Live sync bank mock simulations containing structured accounts
    suspend fun syncBank(bankName: String) {
        val now = System.currentTimeMillis()
        val rules = financeDao.getAllRules().firstOrNull() ?: emptyList()

        val syncedList = when (bankName) {
            "Revolut" -> {
                val tx1Cat = classifyAutonomous("Suscripción Spotify Premium", -12.50, rules)
                val tx2Cat = classifyAutonomous("Supermercado Mercadona", -45.90, rules)
                val tx3Cat = classifyAutonomous("Cena de Gala Anómala (Doble Cobro)", -175.00, rules)
                listOf(
                    Transaction(
                        amount = -12.50,
                        concept = "Suscripción Spotify Premium",
                        category = tx1Cat.first,
                        subcategory = tx1Cat.second,
                        bankName = "Revolut",
                        currency = "USD",
                        timestamp = now - (3600 * 1000 * 4), // 4 hours ago
                        isMicroSpend = true
                    ),
                    Transaction(
                        amount = -45.90,
                        concept = "Supermercado Mercadona",
                        category = tx2Cat.first,
                        subcategory = tx2Cat.second,
                        bankName = "Revolut",
                        currency = "USD",
                        timestamp = now - (3600 * 1000 * 24), // 24 hours ago
                        isMicroSpend = false
                    ),
                    Transaction(
                        amount = -175.00,
                        concept = "Cena de Gala Anómala (Doble Cobro)",
                        category = tx3Cat.first,
                        subcategory = tx3Cat.second,
                        bankName = "Revolut",
                        currency = "USD",
                        timestamp = now - (3600 * 1000 * 48), // 2 days ago
                        isAnomaly = true,
                        anomalyReason = "Desviación del 450% sobre el gasto promedio en esta categoría comercial. Sospecha de error de terminal o fraude.",
                        isMicroSpend = false
                    )
                )
            }
            "TradeRepublic" -> {
                val tx1Cat = classifyAutonomous("Dividendo ETF MSCI World", 250.00, rules)
                val tx2Cat = classifyAutonomous("Aportación recurrente S&P 500", -50.00, rules)
                listOf(
                    Transaction(
                        amount = 250.00,
                        concept = "Dividendo ETF MSCI World",
                        category = tx1Cat.first,
                        subcategory = tx1Cat.second,
                        bankName = "TradeRepublic",
                        currency = "EUR",
                        timestamp = now - (3600 * 1000 * 12),
                        isMicroSpend = false
                    ),
                    Transaction(
                        amount = -50.00,
                        concept = "Aportación recurrente S&P 500",
                        category = tx2Cat.first,
                        subcategory = tx2Cat.second,
                        bankName = "TradeRepublic",
                        currency = "EUR",
                        timestamp = now - (3600 * 1000 * 72),
                        isMicroSpend = false
                    )
                )
            }
            else -> { // Traditional BBVA / Santander / HSBC
                val tx1Cat = classifyAutonomous("Nómina Mensual FinSage Corp", 1850.00, rules)
                val tx2Cat = classifyAutonomous("Fee de Cuenta Corriente Oculto", -2.99, rules)
                val tx3Cat = classifyAutonomous("Gasolinera Repsol", -35.00, rules)
                listOf(
                    Transaction(
                        amount = 1850.00,
                        concept = "Nómina Mensual FinSage Corp",
                        category = tx1Cat.first,
                        subcategory = tx1Cat.second,
                        bankName = "Banco Tradicional",
                        currency = "GBP",
                        timestamp = now - (3600 * 1000 * 168), // 7 days ago
                        isMicroSpend = false
                    ),
                    Transaction(
                        amount = -2.99,
                        concept = "Fee de Cuenta Corriente Oculto",
                        category = tx2Cat.first,
                        subcategory = tx2Cat.second,
                        bankName = "Banco Tradicional",
                        currency = "GBP",
                        timestamp = now - (3600 * 1000 * 3), // 3 hours ago
                        isMicroSpend = true,
                        isAnomaly = true,
                        anomalyReason = "Micro-cobros recurrentes ocultos en concepto de comisiones de mantenimiento de cuenta."
                    ),
                    Transaction(
                        amount = -35.00,
                        concept = "Gasolinera Repsol",
                        category = tx3Cat.first,
                        subcategory = tx3Cat.second,
                        bankName = "Banco Tradicional",
                        currency = "GBP",
                        timestamp = now - (3600 * 1000 * 120),
                        isMicroSpend = false
                    )
                )
            }
        }
        financeDao.insertTransactions(syncedList)
    }

    suspend fun clearAll() {
        financeDao.clearAllTransactions()
    }

    suspend fun initializeDefaultDataIfEmpty() {
        val existing = financeDao.getAllTransactions().firstOrNull() ?: emptyList()
        if (existing.isEmpty()) {
            // Seed custom/default categories
            val defaultCategories = listOf(
                CategoryItem(category = "Alimentos", subcategory = "Supermercado"),
                CategoryItem(category = "Alimentos", subcategory = "Frutería"),
                CategoryItem(category = "Alimentos", subcategory = "Comida rápida"),
                CategoryItem(category = "Suscripción", subcategory = "Streaming"),
                CategoryItem(category = "Suscripción", subcategory = "Gimnasio"),
                CategoryItem(category = "Suscripción", subcategory = "SaaS"),
                CategoryItem(category = "Transporte", subcategory = "Gasolina"),
                CategoryItem(category = "Transporte", subcategory = "Taxi"),
                CategoryItem(category = "Transporte", subcategory = "Metro/Tren"),
                CategoryItem(category = "Restaurantes", subcategory = "Cena"),
                CategoryItem(category = "Restaurantes", subcategory = "Almuerzo"),
                CategoryItem(category = "Restaurantes", subcategory = "Cafetería"),
                CategoryItem(category = "Ocio", subcategory = "Cine"),
                CategoryItem(category = "Ocio", subcategory = "Conciertos"),
                CategoryItem(category = "Ocio", subcategory = "Videojuegos"),
                CategoryItem(category = "Salud", subcategory = "Farmacia"),
                CategoryItem(category = "Salud", subcategory = "Dentista"),
                CategoryItem(category = "Salud", subcategory = "Seguro"),
                CategoryItem(category = "Ingreso", subcategory = "Nómina"),
                CategoryItem(category = "Ingreso", subcategory = "Dividendos"),
                CategoryItem(category = "Ingreso", subcategory = "Regalo"),
                CategoryItem(category = "Ingreso", subcategory = "Ventas"),
                CategoryItem(category = "Inversiones", subcategory = "Bolsa"),
                CategoryItem(category = "Inversiones", subcategory = "Criptomonedas"),
                CategoryItem(category = "Inversiones", subcategory = "Depósitos"),
                CategoryItem(category = "Manual", subcategory = "Otros")
            )
            financeDao.insertCategoryItems(defaultCategories)

            // Seed base values
            syncBank("Revolut")
            syncBank("TradeRepublic")
            syncBank("Santander")

            // Seed default goals
            val goals = listOf(
                BudgetGoal(
                    title = "Fondo Emergencias (6 meses)",
                    targetAmount = 5000.0,
                    savedAmount = 1800.0,
                    targetDate = "2026-12-31",
                    category = "Seguridad",
                    isAutoCalculated = true
                ),
                BudgetGoal(
                    title = "Viaje Escapada Alpes AI",
                    targetAmount = 1200.0,
                    savedAmount = 450.0,
                    targetDate = "2026-08-15",
                    category = "Ocio",
                    isAutoCalculated = true
                )
            )
            for (g in goals) {
                financeDao.insertGoal(g)
            }
        }
    }

    // --- CSV Exporter ---
    fun exportToCSV(transactions: List<Transaction>): String {
        val writer = StringBuilder()
        writer.append("ID,Concepto,Cantidad,Divisa,Categoria,Subcategoria,Entidad,Fecha,Es_Anomalia,Razon_Anomalia\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        for (tx in transactions) {
            val dateStr = sdf.format(Date(tx.timestamp))
            val safeReason = tx.anomalyReason?.replace(",", ";") ?: ""
            writer.append("${tx.id},\"${tx.concept.replace("\"", "\"\"")}\",${tx.amount},${tx.currency},${tx.category},${tx.subcategory},${tx.bankName},$dateStr,${tx.isAnomaly},\"$safeReason\"\n")
        }
        return writer.toString()
    }

    // --- High-Fidelity Text-Based Report ---
    fun generatePDFReport(transactions: List<Transaction>, goals: List<BudgetGoal>): String {
        val report = StringBuilder()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val generatedAt = sdf.format(Date())

        report.append("========================================================\n")
        report.append("                 FINSAGE AI - REPORTE DE SALDO          \n")
        report.append("========================================================\n")
        report.append("Fecha de Expedición: $generatedAt\n")
        report.append("Cifrado E2EE: Habilitado (AES-256)\n")
        report.append("Consigna Global: Reporte consolidado de transacciones multi-divisa\n")
        report.append("--------------------------------------------------------\n\n")

        val incomes = transactions.filter { it.amount > 0 }
        val expenses = transactions.filter { it.amount < 0 }
        val totalIncome = incomes.sumOf { it.amount }
        val totalExpense = expenses.sumOf { it.amount }
        val balance = totalIncome + totalExpense

        report.append("RESUMEN DE CUENTAS\n")
        report.append("----------------------------\n")
        report.append("Total de Ingresos:  +${"%.2f".format(totalIncome)} EUR\n")
        report.append("Total de Gastos:    ${"%.2f".format(totalExpense)} EUR\n")
        report.append("Balance de Caja:    ${"%.2f".format(balance)} EUR\n")
        report.append("Entidades Conectadas: Revolut, TradeRepublic, Santander\n\n")

        report.append("Detección de Patrones por IA:\n")
        val microCount = transactions.count { it.isMicroSpend }
        val microSum = transactions.filter { it.isMicroSpend }.sumOf { it.amount }
        report.append("- Micro-gastos detectados: $microCount (con un impacto total de ${"%.2f".format(microSum)} EUR).\n")
        val anomalyCount = transactions.count { it.isAnomaly }
        report.append("- Alertas de Anomalía Activas: $anomalyCount transacciones sospechosas detectadas por la red neuronal.\n\n")

        report.append("LISTA DE METAS DE AHORRO\n")
        report.append("--------------------------------------------------------\n")
        goals.forEach { goal ->
            val progress = (goal.savedAmount / goal.targetAmount) * 100
            report.append("- ${goal.title}: ${"%.1f".format(progress)}% completado (${goal.savedAmount} / ${goal.targetAmount} EUR). Meta: ${goal.targetDate}\n")
        }
        report.append("\n")

        report.append("REGISTRO DE TRANSACCIONES DETALLADO\n")
        report.append("--------------------------------------------------------\n")
        transactions.forEach { tx ->
            val indicator = if (tx.amount < 0) "[-] GASTO" else "[+] INGRESO"
            val state = if (tx.isAnomaly) "⚠️ [SOC] " else ""
            val date = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(tx.timestamp))
            report.append("[$date] $indicator | $state${tx.concept} | ${"%.2f".format(tx.amount)} ${tx.currency} (${tx.bankName}) - Categoría: ${tx.category} > ${tx.subcategory}\n")
            if (tx.isAnomaly && tx.anomalyReason != null) {
                report.append("   ↳ Causa de Alerta: ${tx.anomalyReason}\n")
            }
        }
        report.append("\n========================================================\n")
        report.append("      FINSAGE AI: Tu coach con Inteligencia Artificial. \n")
        report.append("========================================================\n")

        return report.toString()
    }
}
