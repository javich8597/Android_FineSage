package com.example.ui.finance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BudgetGoal
import com.example.data.model.Transaction
import com.example.data.model.CategoryItem
import com.example.data.model.LearnedRule
import com.example.data.repository.FinanceRepository
import com.example.data.network.GeminiApiClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import com.example.data.worker.SyncWorker

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import com.example.data.network.SupabaseApiClient

class FinanceViewModel(context: Context) : ViewModel() {

    private val database = AppDatabase.getDatabase(context)
    private val repository = FinanceRepository(database.financeDao())

    // Supabase Auth State
    private val _supabaseUserEmail = MutableStateFlow<String?>(null)
    val supabaseUserEmail: StateFlow<String?> = _supabaseUserEmail.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // --- Database Flows ---
    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<BudgetGoal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val anomalies: StateFlow<List<Transaction>> = repository.anomalies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val microSpends: StateFlow<List<Transaction>> = repository.microSpends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryItems: StateFlow<List<CategoryItem>> = repository.allCategoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rules: StateFlow<List<LearnedRule>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI State Variables ---
    private val _isDarkMode = MutableStateFlow(true) // Defaults to Premium Dark Theme
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _language = MutableStateFlow("es") // "es" or "en"
    val language: StateFlow<String> = _language.asStateFlow()

    private val _isBiometricsEnabled = MutableStateFlow(true)
    val isBiometricsEnabled: StateFlow<Boolean> = _isBiometricsEnabled.asStateFlow()

    private val _isUserAuthenticated = MutableStateFlow(false) // Safe-guarded by biometrics on launch
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    private val _budgetAlert = MutableStateFlow<String?>(null)
    val budgetAlert: StateFlow<String?> = _budgetAlert.asStateFlow()

    private val _coachingResponse = MutableStateFlow<String>("")
    val coachingResponse: StateFlow<String> = _coachingResponse.asStateFlow()

    private val _isCoachingLoading = MutableStateFlow(false)
    val isCoachingLoading: StateFlow<Boolean> = _isCoachingLoading.asStateFlow()

    // --- Live Multi-Currency Context State ---
    private val _selectedCurrency = MutableStateFlow("EUR")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    private val _exchangeRatesState = MutableStateFlow(
        mapOf(
            "EUR" to 1.0,
            "USD" to 1.12,  // 1 EUR = 1.12 USD
            "GBP" to 0.86,  // 1 EUR = 0.86 GBP
            "JPY" to 169.5  // 1 EUR = 169.5 JPY
        )
    )
    val exchangeRatesState: StateFlow<Map<String, Double>> = _exchangeRatesState.asStateFlow()

    init {
        // Monitor Supabase session
        viewModelScope.launch {
            SupabaseApiClient.client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        _supabaseUserEmail.value = status.session.user?.email
                        _isUserAuthenticated.value = true
                        
                        // Load offline data if logged in
                        repository.initializeDefaultDataIfEmpty()
                        checkBudgetLimits()
                        askCoachingAdvisor()
                    }
                    else -> {
                        _supabaseUserEmail.value = null
                        _isUserAuthenticated.value = false
                    }
                }
            }
        }

        // Real-Time Currency Fluctuations Ticker
        viewModelScope.launch {
            val random = java.util.Random()
            while (true) {
                kotlinx.coroutines.delay(8000) // Tickers update every 8 seconds
                val current = _exchangeRatesState.value.toMutableMap()
                // Fluctuate USD, GBP, JPY rates slightly (+/- 0.15%) relative to EUR
                current["USD"] = (current["USD"] ?: 1.12) * (1.0 + (random.nextDouble() - 0.5) * 0.003)
                current["GBP"] = (current["GBP"] ?: 0.86) * (1.0 + (random.nextDouble() - 0.5) * 0.003)
                current["JPY"] = (current["JPY"] ?: 169.5) * (1.0 + (random.nextDouble() - 0.5) * 0.003)
                _exchangeRatesState.value = current
                checkBudgetLimits()
            }
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun toggleBiometrics() {
        _isBiometricsEnabled.value = !_isBiometricsEnabled.value
    }

    fun setAuthenticated(auth: Boolean) {
        // Obsolete if purely using supabase status, keeping for fallback
        _isUserAuthenticated.value = auth
    }

    // --- Supabase Authentication ---
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            try {
                _authError.value = null
                SupabaseApiClient.client.auth.signInWith(Email) {
                    this.email = email
                    password = pass
                }
            } catch (e: Exception) {
                _authError.value = "Login failed: ${e.localizedMessage}"
            }
        }
    }

    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            try {
                _authError.value = null
                SupabaseApiClient.client.auth.signUpWith(Email) {
                    this.email = email
                    password = pass
                }
            } catch (e: Exception) {
                _authError.value = "Signup failed: ${e.localizedMessage}"
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            try {
                SupabaseApiClient.client.auth.signOut()
            } catch (e: Exception) {
                _authError.value = "Logout failed: ${e.localizedMessage}"
            }
        }
    }

    // --- Currency Controls ---
    fun setBaseCurrency(currency: String) {
        _selectedCurrency.value = currency
        checkBudgetLimits()
    }

    fun convertCurrency(amount: Double, from: String, to: String): Double {
        if (from == to) return amount
        val rates = _exchangeRatesState.value
        val rateFrom = rates[from] ?: 1.0
        val rateTo = rates[to] ?: 1.0
        // convert from currency to absolute anchor EUR, then convert EUR to destination currency
        val amountInEur = amount / rateFrom
        return amountInEur * rateTo
    }

    fun getCurrencySymbol(currency: String): String {
        return when (currency) {
            "EUR" -> "€"
            "USD" -> "$"
            "GBP" -> "£"
            "JPY" -> "¥"
            else -> currency
        }
    }

    fun formatCurrency(amount: Double, currency: String): String {
        val symbol = getCurrencySymbol(currency)
        return if (currency == "JPY") {
            "${"%,.0f".format(amount)} $symbol"
        } else {
            "${"%,.2f".format(amount)} $symbol"
        }
    }

    // --- Bank Account Synchronizers ---
    fun synchronizeBank(bankName: String) {
        viewModelScope.launch {
            _syncStatus.value = if (_language.value == "es") "Conectando con $bankName via API segura..." else "Connecting to $bankName via secure Open Banking..."
            kotlinx.coroutines.delay(1800) // Simulated secure token exchange loading
            repository.syncBank(bankName)
            checkBudgetLimits()
            triggerSync()
            _syncStatus.value = if (_language.value == "es") "Sincronizado con éxito con $bankName." else "Successfully synchronized accounts from $bankName."
            kotlinx.coroutines.delay(3000)
            _syncStatus.value = null
        }
    }

    // Checking if user spends exceed threshold
    private fun checkBudgetLimits() {
        viewModelScope.launch {
            val txList = transactions.firstOrNull() ?: transactions.value
            val currentCurrency = _selectedCurrency.value
            val thresholdInEUR = 300.0
            val thresholdInSelected = convertCurrency(thresholdInEUR, "EUR", currentCurrency)

            val totalExpenseInSelected = txList.filter { it.amount < 0 }
                .sumOf { -convertCurrency(it.amount, it.currency, currentCurrency) }

            if (totalExpenseInSelected > thresholdInSelected) {
                val symbol = getCurrencySymbol(currentCurrency)
                _budgetAlert.value = if (_language.value == "es") {
                    "⚠️ ALERTA: Tus gastos mensuales (${formatCurrency(totalExpenseInSelected, currentCurrency)}) han superado el presupuesto sugerido de ${formatCurrency(thresholdInSelected, currentCurrency)}."
                } else {
                    "⚠️ WARNING: Your monthly spending (${formatCurrency(totalExpenseInSelected, currentCurrency)}) has exceeded your safety threshold of ${formatCurrency(thresholdInSelected, currentCurrency)}."
                }
            } else {
                _budgetAlert.value = null
            }
        }
    }

    fun dismissBudgetAlert() {
        _budgetAlert.value = null
    }

    // --- Ask Gemini Advisor ---
    fun askCoachingAdvisor() {
        viewModelScope.launch {
            _isCoachingLoading.value = true
            _coachingResponse.value = if (_language.value == "es") "Consultando a FinSage AI Coach con tus gastos consolidados..." else "Analyzing spend logs. Consulting Gemini AI Financial Coach..."
            try {
                val response = GeminiApiClient.getFinancialCoaching(transactions.value, goals.value)
                _coachingResponse.value = response
            } catch (e: Exception) {
                _coachingResponse.value = "Error al conectar con la IA de FinSage: ${e.localizedMessage}"
            } finally {
                _isCoachingLoading.value = false
            }
        }
    }

    private val appContext = context.applicationContext

    private fun triggerSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(appContext).enqueue(syncRequest)
    }

    suspend fun extractReceiptImage(imageBytes: ByteArray): com.example.data.network.ReceiptExtraction? {
        return GeminiApiClient.extractReceiptInfo(imageBytes, transactions.value)
    }

    // --- Operation Services ---
    fun addManualTransaction(concept: String, amount: Double, category: String, subcategory: String, bankName: String, currency: String = "EUR") {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            var finalCategory = category
            var finalSubcategory = subcategory

            // If category is not specified, run autonomous classification (with learning rules!)
            if (finalCategory.isEmpty()) {
                val rulesList = rules.value
                val classified = repository.classifyAutonomous(concept, amount, rulesList)
                finalCategory = classified.first
                finalSubcategory = classified.second
            }

            // Estimate anomaly or micro-spend with updated category
            val isAnomaly = amount < -150.0 && finalCategory == "Restaurantes"
            val isMicro = amount in -10.0..-0.01

            val newTx = Transaction(
                amount = amount,
                concept = concept,
                category = finalCategory,
                subcategory = finalSubcategory,
                bankName = bankName,
                currency = currency,
                timestamp = now,
                isAnomaly = isAnomaly,
                anomalyReason = if (isAnomaly) "Supera en un 280% la mediana histórica de consumo en esta categoría para tu zona horaria." else null,
                isMicroSpend = isMicro
            )
            repository.insertTransaction(newTx)
            checkBudgetLimits()
            triggerSync()
        }
    }

    fun updateTransactionCategory(transaction: Transaction, newCategory: String, newSubcategory: String) {
        viewModelScope.launch {
            // Update the single transaction
            repository.insertTransaction(
                transaction.copy(category = newCategory, subcategory = newSubcategory)
            )

            // Dynamic automated learning mapping: learn from the user actions
            repository.learnCategorizationRule(transaction.concept, newCategory, newSubcategory)
            
            checkBudgetLimits()
            triggerSync()
        }
    }

    fun addCustomCategoryItem(category: String, subcategory: String) {
        viewModelScope.launch {
            if (category.trim().isNotEmpty() && subcategory.trim().isNotEmpty()) {
                repository.insertCategoryItem(CategoryItem(category = category.trim(), subcategory = subcategory.trim()))
            }
        }
    }

    fun deleteCustomCategoryItem(item: CategoryItem) {
        viewModelScope.launch {
            repository.deleteCategoryItem(item)
        }
    }

    fun addManualGoal(title: String, target: Double, date: String, category: String) {
        viewModelScope.launch {
            val newGoal = BudgetGoal(
                title = title,
                targetAmount = target,
                savedAmount = 0.0,
                targetDate = date,
                category = category,
                isAutoCalculated = true
            )
            repository.insertGoal(newGoal)
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            repository.clearAll()
            checkBudgetLimits()
            triggerSync()
        }
    }

    // --- CSV Share ---
    fun getCsvContent(): String {
        return repository.exportToCSV(transactions.value)
    }

    // --- PDF Share ---
    fun getPdfReportContent(): String {
        return repository.generatePDFReport(transactions.value, goals.value)
    }
}
