package com.example.ui.finance

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BudgetGoal
import com.example.data.model.Transaction
import com.example.data.model.CategoryItem
import com.example.data.model.LearnedRule
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- SOPHISTICATED GRID PATTERN EXTENSION ---
fun Modifier.sophisticatedDotMesh(isDarkMode: Boolean): Modifier = this.drawBehind {
    if (isDarkMode) {
        val dotRadius = 1.2f
        val spacingPx = 24.dp.toPx()
        val color = Color(0xFFD1E4FF).copy(alpha = 0.05f)
        var x = spacingPx / 2
        while (x < size.width) {
            var y = spacingPx / 2
            while (y < size.height) {
                drawCircle(color, radius = dotRadius, center = Offset(x, y))
                y += spacingPx
            }
            x += spacingPx
        }
    }
}


// --- MULTILANGUAGE STRINGS ---
val Localization = mapOf(
    "es" to mapOf(
        "app_tag" to "COACH CON INTELIGENCIA ARTIFICIAL",
        "bio_lock_title" to "ACCESO RESGUARDADO FINSAGE",
        "bio_lock_subtitle" to "Cifrado de Extremo a Extremo Habilitado",
        "bio_lock_desc" to "Esta aplicación contiene credenciales bancarias sincronizadas con Revolut y TradeRepublic de forma confidencial. Autentíquese biométricamente para continuar.",
        "bio_btn" to "Simular Huella Dactilar",
        "bio_success" to "¡Acceso concedido!",
        "nav_resumen" to "Resumen",
        "nav_charts" to "Gráficas",
        "nav_coach" to "AI Coach",
        "nav_metas" to "Metas",
        "nav_config" to "Panel",
        "card_total" to "Balance Estimado",
        "card_income" to "Ingresos",
        "card_expense" to "Gastos",
        "section_sync" to "Sincronización Bancaria Global",
        "sync_now" to "Sincronizar",
        "syncing" to "Estableciendo conexión...",
        "status_connected" to "Sincronizado",
        "status_disconnected" to "Desconectado",
        "section_anomalies" to "Alertas de Anomalía Neural (GNN)",
        "anomaly_warning" to "Anomalías detectadas",
        "section_micro" to "Análisis De Micro-gastos",
        "micro_title" to "Gastos Hormiga Detectados",
        "micro_total_impact" to "Impacto total",
        "recent_tx" to "Últimas Transacciones",
        "no_tx" to "No hay transacciones registradas.",
        "add_tx" to "Añadir Transacción",
        "btn_add" to "Añadir",
        "lbl_concept" to "Concepto (ej. Netflix)",
        "lbl_amount" to "Cantidad (€ - usar negativo para gasto)",
        "lbl_category" to "Categoría",
        "lbl_bank" to "Banco / Entidad",
        "coach_title" to "FinSage IA Asesor Financiero",
        "coach_desc" to "Tu coach analiza tu cuenta bancaria de forma automatizada y predice pérdidas antes de que ocurran.",
        "ask_coach_btn" to "Generar Informe con Gemini 3.5",
        "metas_title" to "Metas y Objetivos de Ahorro",
        "metas_desc" to "Aportes automatizados sugeridos según histórico transaccional.",
        "add_goal" to "Añadir Nueva Meta",
        "lbl_goal_title" to "Título de la Meta (ej. Fondo de Emergencias)",
        "lbl_goal_target" to "Monto Objetivo (€)",
        "lbl_goal_date" to "Fecha límite (AAAA-MM-DD)",
        "config_security" to "Seguridad y Ajustes",
        "config_biometrics" to "Autenticación Biométrica",
        "config_darkmode" to "Modo Oscuro Premium",
        "config_language" to "Idioma / Language",
        "config_danger_zone" to "Zona de Peligro",
        "config_delete_all" to "Restablecer Base de Datos (Limpiar)",
        "export_csv" to "Exportar CSV Inmediato",
        "export_pdf" to "Exportar PDF Consolidado",
        "copied_clipboard" to "Copiado al portapapeles",
        "cancel" to "Cancelar",
        "currency_selector" to "Divisa Principal"
    ),
    "en" to mapOf(
        "app_tag" to "AI PERSONAL FINANCIAL COACH",
        "bio_lock_title" to "FINSAGE SECURE ENTRANCE",
        "bio_lock_subtitle" to "End-to-End Encryption Enabled",
        "bio_lock_desc" to "This application processes secure synchronized credentials from Revolut and TradeRepublic confidentially. Authenticate to view dashboard.",
        "bio_btn" to "Simulate Fingerprint Scan",
        "bio_success" to "Access Granted!",
        "nav_resumen" to "Dashboard",
        "nav_charts" to "Charts",
        "nav_coach" to "AI Coach",
        "nav_metas" to "Goals",
        "nav_config" to "Settings",
        "card_total" to "Estimated Balance",
        "card_income" to "Total Income",
        "card_expense" to "Total Expense",
        "section_sync" to "Global Open Banking Sync",
        "sync_now" to "Sync Bank",
        "syncing" to "Establishing secure connection...",
        "status_connected" to "Synchronized",
        "status_disconnected" to "Disconnected",
        "section_anomalies" to "Neural Anomaly Alerts (GNN)",
        "anomaly_warning" to "Anomalies detected",
        "section_micro" to "Micro-Spends Intelligence Log",
        "micro_title" to "Micro-spends Detected",
        "micro_total_impact" to "Total impact",
        "recent_tx" to "Recent Transactions",
        "no_tx" to "No transactions logged yet.",
        "add_tx" to "Add Transaction",
        "btn_add" to "Add Cash Flow",
        "lbl_concept" to "Concept (e.g. Netflix)",
        "lbl_amount" to "Amount ($ - enter negative for budget spent)",
        "lbl_category" to "Category",
        "lbl_bank" to "Bank Account",
        "coach_title" to "FinSage AI Advisor",
        "coach_desc" to "Your coach analyzes bank logs on the fly and forecasts budget leaks before they occur.",
        "ask_coach_btn" to "Analyze with Gemini 3.5 Flash",
        "metas_title" to "Personal Goals & Targets",
        "metas_desc" to "Heuristic savings calculated directly from real-life spending habits.",
        "add_goal" to "Create Saving Goal",
        "lbl_goal_title" to "Goal Name (e.g. Emergency Fund)",
        "lbl_goal_target" to "Milestone Amount (€)",
        "lbl_goal_date" to "Target Date (YYYY-MM-DD)",
        "config_security" to "Security & Adjustments",
        "config_biometrics" to "Biometric Authentication",
        "config_darkmode" to "Premium Dark Mode",
        "config_language" to "Idioma / Language",
        "config_danger_zone" to "Danger Zone",
        "config_delete_all" to "Reset local SQLite database",
        "export_csv" to "Instant CSV Export",
        "export_pdf" to "Instant PDF Report",
        "copied_clipboard" to "Copied to Clipboard",
        "cancel" to "Cancel",
        "currency_selector" to "Preferred Currency"
    )
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FinanceAppScreen(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val language by viewModel.language.collectAsState()
    val isBiometricsEnabled by viewModel.isBiometricsEnabled.collectAsState()
    val isUserAuthenticated by viewModel.isUserAuthenticated.collectAsState()

    // Setup active translation bundle
    val labels = remember(language) { Localization[language] ?: Localization["es"]!! }

    var currentTab by remember { mutableStateOf("resumen") } // "resumen", "coach", "metas", "config"

    // Dialog state
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    // Custom background brush based on Selected theme (Sophisticated Dark vs Clean Dynamic Light)
    val backgroundBrush = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0F1113), Color(0xFF16191D))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFAFBFD), Color(0xFFEFEFF4))
        )
    }

    val contentColor = if (isDarkMode) Color(0xFFE2E2E6) else Color(0xFF151515)

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        bottomBar = {
            if (isUserAuthenticated) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDarkMode) Color(0xFF1A1C1E) else Color(0xFFF0F3F9))
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    if (isDarkMode) {
                        Divider(color = Color(0xFF2D3135), thickness = 1.dp)
                    }
                    NavigationBar(
                        containerColor = if (isDarkMode) Color(0xFF1A1C1E) else Color(0xFFF0F3F9),
                        tonalElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NavigationBarItem(
                            selected = currentTab == "resumen",
                            onClick = { currentTab = "resumen" },
                            icon = { Icon(Icons.Filled.AccountBalanceWallet, null) },
                            label = { Text(labels["nav_resumen"] ?: "") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_item_overview")
                        )
                        NavigationBarItem(
                            selected = currentTab == "coach",
                            onClick = { currentTab = "coach" },
                            icon = { Icon(Icons.Filled.Psychology, null) },
                            label = { Text(labels["nav_coach"] ?: "") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_item_coach")
                        )
                        NavigationBarItem(
                            selected = currentTab == "metas",
                            onClick = { currentTab = "metas" },
                            icon = { Icon(Icons.Filled.Flag, null) },
                            label = { Text(labels["nav_metas"] ?: "") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_item_goals")
                        )
                        NavigationBarItem(
                            selected = currentTab == "historial",
                            onClick = { currentTab = "historial" },
                            icon = { Icon(Icons.Filled.History, null) },
                            label = { Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Historial" else "History") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_item_history")
                        )
                        NavigationBarItem(
                            selected = currentTab == "graficas",
                            onClick = { currentTab = "graficas" },
                            icon = { Icon(Icons.Filled.BarChart, null) },
                            label = { Text(labels["nav_charts"] ?: "") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_item_charts")
                        )
                        NavigationBarItem(
                            selected = currentTab == "config",
                            onClick = { currentTab = "config" },
                            icon = { Icon(Icons.Filled.Tune, null) },
                            label = { Text(labels["nav_config"] ?: "") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_item_settings")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isUserAuthenticated && currentTab == "resumen") {
                FloatingActionButton(
                    onClick = { showAddTxDialog = true },
                    containerColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                    contentColor = if (isDarkMode) Color(0xFF003258) else Color.White,
                    modifier = Modifier
                        .testTag("add_trans_fab")
                        .padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = labels["add_tx"])
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .sophisticatedDotMesh(isDarkMode)
                .padding(paddingValues)
        ) {
            // --- SECURITY LOCK OVERLAY (BIOMETRIC) ---
            if (isBiometricsEnabled && !isUserAuthenticated) {
                BiometricLockScreen(
                    labels = labels,
                    isDarkMode = isDarkMode,
                    onSuccess = {
                        viewModel.setAuthenticated(true)
                        Toast.makeText(context, labels["bio_success"], Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // Main Application Contents
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = spring()) with fadeOut(animationSpec = spring())
                    },
                    modifier = Modifier.fillMaxSize()
                ) { targetState ->
                    when (targetState) {
                        "resumen" -> DashboardTab(
                            viewModel = viewModel,
                            labels = labels,
                            isDarkMode = isDarkMode,
                            onSyncClick = { bank -> viewModel.synchronizeBank(bank) }
                        )
                        "coach" -> CoachTab(
                            viewModel = viewModel,
                            labels = labels,
                            isDarkMode = isDarkMode
                        )
                        "metas" -> GoalsTab(
                            viewModel = viewModel,
                            labels = labels,
                            isDarkMode = isDarkMode,
                            onAddGoalClick = { showAddGoalDialog = true }
                        )
                        "historial" -> HistoryTab(
                            viewModel = viewModel,
                            labels = labels,
                            isDarkMode = isDarkMode
                        )
                        "graficas" -> ChartsTab(
                            viewModel = viewModel,
                            labels = labels,
                            isDarkMode = isDarkMode
                        )
                        "config" -> ManagementTab(
                            viewModel = viewModel,
                            labels = labels,
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS FOR MUTATIONS ---
    if (showAddTxDialog) {
        AddTransactionModal(
            labels = labels,
            onDismiss = { showAddTxDialog = false },
            onConfirm = { concept, amount, category, bank, currency ->
                viewModel.addManualTransaction(concept, amount, category, "", bank, currency)
                showAddTxDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalModal(
            labels = labels,
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, target, date, category ->
                viewModel.addManualGoal(title, target, date, category)
                showAddGoalDialog = false
            }
        )
    }
}

// --- BIOMETRIC SECURITY COMPONENT ---
@Composable
fun BiometricLockScreen(
    labels: Map<String, String>,
    isDarkMode: Boolean,
    onSuccess: () -> Unit
) {
    var isChecking by remember { mutableStateOf(false) }
    val scannerColor by animateColorAsState(
        targetValue = if (isChecking) Color(0xFFBAC3FF) else Color.Gray,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .sophisticatedDotMesh(isDarkMode)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Safe lock shield logo
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(if (isDarkMode) Color(0xFF1A1C1E) else Color(0xFFF0F4FA))
                .border(1.dp, if (isDarkMode) Color(0xFF2D3135) else Color.Transparent, RoundedCornerShape(30.dp))
                .clickable {
                    isChecking = true
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Fingerprint,
                contentDescription = null,
                tint = if (isChecking) scannerColor else (if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            labels["bio_lock_title"] ?: "",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color.White else Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Https,
                contentDescription = null,
                tint = if (isDarkMode) Color(0xFFBAC3FF) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                labels["bio_lock_subtitle"] ?: "",
                fontSize = 12.sp,
                color = if (isDarkMode) Color(0xFFC2C7CF) else Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            labels["bio_lock_desc"] ?: "",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                isChecking = true
                onSuccess()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(48.dp)
                .testTag("biometric_trigger_button")
        ) {
            Text(labels["bio_btn"] ?: "", fontWeight = FontWeight.Bold)
        }
    }
}


// --- TAB 1: DASHBOARD COMPONENT ---
@Composable
fun DashboardTab(
    viewModel: FinanceViewModel,
    labels: Map<String, String>,
    isDarkMode: Boolean,
    onSyncClick: (String) -> Unit
) {
    val txList by viewModel.transactions.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val budgetAlert by viewModel.budgetAlert.collectAsState()
    val microSpends by viewModel.microSpends.collectAsState()
    val anomalies by viewModel.anomalies.collectAsState()

    val selectCurrency by viewModel.selectedCurrency.collectAsState()
    val liveRates by viewModel.exchangeRatesState.collectAsState()

    val totalSpent = remember(txList, selectCurrency, liveRates) {
        txList.filter { it.amount < 0 }
            .sumOf { viewModel.convertCurrency(it.amount, it.currency, selectCurrency) }
    }
    val totalIncome = remember(txList, selectCurrency, liveRates) {
        txList.filter { it.amount > 0 }
            .sumOf { viewModel.convertCurrency(it.amount, it.currency, selectCurrency) }
    }
    val netBalance = totalIncome + totalSpent

    val revolutBalance = remember(txList, selectCurrency, liveRates) {
        txList.filter { it.bankName == "Revolut" }
            .sumOf { viewModel.convertCurrency(it.amount, it.currency, selectCurrency) }
    }
    val tradeRepublicBalance = remember(txList, selectCurrency, liveRates) {
        txList.filter { it.bankName == "TradeRepublic" }
            .sumOf { viewModel.convertCurrency(it.amount, it.currency, selectCurrency) }
    }
    val santanderBalance = remember(txList, selectCurrency, liveRates) {
        txList.filter { it.bankName == "Banco Tradicional" }
            .sumOf { viewModel.convertCurrency(it.amount, it.currency, selectCurrency) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_scroll_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Brand inspired by "Sophisticated Dark"
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Hola, " else "Hello, ",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Light,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                        Text(
                            text = "Javier",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    }
                    Text(
                        text = (labels["app_tag"] ?: "").uppercase(),
                        fontSize = 10.sp,
                        color = if (isDarkMode) Color(0xFFBAC3FF) else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                }

                // JS Avatar Circle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF3F4759) else Color(0xFFE5F1FD))
                        .border(1.dp, if (isDarkMode) Color(0xFF43474E) else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JS",
                        color = if (isDarkMode) Color.White else MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // BUDGET PUSH NOTIFICATION ALERT
        if (budgetAlert != null) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFECEF),
                        contentColor = Color(0xFFBC1C32)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Dangerous, null, tint = Color(0xFFE22B43), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = budgetAlert!!,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = { viewModel.dismissBudgetAlert() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }
                }
            }
        }

        // Sync State Dynamic Banner
        if (syncStatus != null) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF1E2F40) else Color(0xFFE4F3FF),
                        contentColor = if (isDarkMode) Color(0xFF4AC2FF) else Color(0xFF0D5EAF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = if (isDarkMode) Color(0xFF4AC2FF) else Color(0xFF0D5EAF),
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = syncStatus!!,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ESTADÍSTICAS BALANCES CARD styled as the main gradient card in Sophisticated Dark
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color.Transparent else Color.White
                ),
                border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isDarkMode) Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1B2B41), Color(0xFF111418))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) else Modifier
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            labels["card_total"] ?: "",
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color(0xFFD1E4FF) else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDarkMode) Color(0xFF004A77) else Color(0xFFE5F1FD))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "REVOLUT SYNC",
                                fontSize = 8.sp,
                                color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        viewModel.formatCurrency(netBalance, selectCurrency),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (netBalance >= 0) (if (isDarkMode) Color.White else Color(0xFF107C41)) else Color(0xFFD13438)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    // AI Tip Box inspired by "Sophisticated Dark"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDarkMode) Color(0x0DFFFFFF) else Color(0x06000000))
                            .border(1.dp, if (isDarkMode) Color(0x1AFFFFFF) else Color(0x0D000000), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDarkMode) Color(0xFFBAC3FF) else MaterialTheme.colorScheme.primary.copy(0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF1A1C1E) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") {
                                    "Tip de IA: Tus microgastos en suscripciones han bajado un 12.5% este mes. ¡Buen camino!"
                                } else {
                                    "AI Tip: Your micro-spending on subscriptions is down 12.5% this month. Keep it up!"
                                },
                                fontSize = 11.sp,
                                color = if (isDarkMode) Color(0xFFC2C7CF) else Color(0xFF43474E),
                                lineHeight = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = if (isDarkMode) Color(0xFF2D3135) else Color(0xFFE5E5E5))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF107C41))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    labels["card_income"] ?: "",
                                    fontSize = 12.sp,
                                    color = if (isDarkMode) Color(0xFFC2C7CF) else Color.Gray
                                )
                            }
                            Text(
                                "+" + viewModel.formatCurrency(totalIncome, selectCurrency),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD13438))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    labels["card_expense"] ?: "",
                                    fontSize = 12.sp,
                                    color = if (isDarkMode) Color(0xFFC2C7CF) else Color.Gray
                                )
                            }
                            Text(
                                viewModel.formatCurrency(totalSpent, selectCurrency),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }

        // SECCIÓN SINCRO BANCARIA REAL-TIME
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    labels["section_sync"] ?: "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BankConnectItem(
                        bankName = "Revolut",
                        labels = labels,
                        avatarLetter = "R",
                        avatarBg = Color(0xFF0052FF),
                        isDarkMode = isDarkMode,
                        isConnected = txList.any { it.bankName == "Revolut" },
                        balanceText = if (txList.any { it.bankName == "Revolut" }) viewModel.formatCurrency(revolutBalance, selectCurrency) else null,
                        onSync = { onSyncClick("Revolut") }
                    )
                    BankConnectItem(
                        bankName = "TradeRepublic",
                        labels = labels,
                        avatarLetter = "T",
                        avatarBg = Color.Black,
                        isDarkMode = isDarkMode,
                        isConnected = txList.any { it.bankName == "TradeRepublic" },
                        balanceText = if (txList.any { it.bankName == "TradeRepublic" }) viewModel.formatCurrency(tradeRepublicBalance, selectCurrency) else null,
                        onSync = { onSyncClick("TradeRepublic") }
                    )
                    BankConnectItem(
                        bankName = "Santander",
                        labels = labels,
                        avatarLetter = "S",
                        avatarBg = Color(0xFFEC0000),
                        isDarkMode = isDarkMode,
                        isConnected = txList.any { it.bankName == "Banco Tradicional" },
                        balanceText = if (txList.any { it.bankName == "Banco Tradicional" }) viewModel.formatCurrency(santanderBalance, selectCurrency) else null,
                        onSync = { onSyncClick("Santander") }
                    )
                }
            }
        }

        // ANOMALY INTEGRITY ZONE (HEURISTIC GRAPH NEURAL NETWORK VISUALIZATION)
        if (anomalies.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF2B141B) else Color(0xFFFFF2F4)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE22B43)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, null, tint = Color(0xFFE22B43), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                labels["section_anomalies"] ?: "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE22B43)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "${labels["anomaly_warning"] ?: ""}: ${anomalies.size} transacciones sospechosas detectadas por la IA local basada en GNN (Graph Neural Network).",
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        anomalies.forEach { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.concept, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isDarkMode) Color.White else Color.Black)
                                    Text(tx.anomalyReason ?: "", fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                val convertedAnomaly = viewModel.convertCurrency(tx.amount, tx.currency, selectCurrency)
                                Text(viewModel.formatCurrency(convertedAnomaly, selectCurrency), fontSize = 13.sp, color = Color(0xFFE22B43), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // GRAPH WAVE CANVAS SECTION styled with GNN mesh patterns
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1A1C1E) else Color.White
                ),
                border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Análisis de Patrones (GNN)" else "Patterns Analysis (GNN)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isDarkMode) Color(0x33D1E4FF) else Color(0xFFE5F1FD))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "REAL-TIME SCAN",
                                fontSize = 8.sp,
                                color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Mapeado heurístico de dependencias financieras" else "Heuristic mapping of financial dependencies",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Wave Chart custom Canvas drawing with GNN overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .sophisticatedDotMesh(isDarkMode)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height

                            // Draw a light decorative neural grid lines in background
                            if (isDarkMode) {
                                val nodes = listOf(
                                    Offset(width * 0.25f, height * 0.3f),
                                    Offset(width * 0.75f, height * 0.45f),
                                    Offset(width * 0.55f, height * 0.8f),
                                    Offset(width * 0.15f, height * 0.7f)
                                )
                                drawLine(Color(0x1AD1E4FF), nodes[0], nodes[1], strokeWidth = 1f)
                                drawLine(Color(0x1AD1E4FF), nodes[1], nodes[2], strokeWidth = 1f)
                                drawLine(Color(0x1AD1E4FF), nodes[0], nodes[2], strokeWidth = 1f)
                                drawCircle(Color(0x2BD1E4FF), radius = 6f, center = nodes[0])
                                drawCircle(Color(0x2BBAC3FF), radius = 8f, center = nodes[2])
                            }

                            // Draw baseline axis lines
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.2f),
                                start = Offset(0f, height * 0.8f),
                                end = Offset(width, height * 0.8f),
                                strokeWidth = 1.5f
                            )

                            if (txList.isNotEmpty()) {
                                val points = txList.take(8).mapIndexed { idx, tx ->
                                    val x = width - (idx * (width / 7))
                                    // map amount to Y bounds
                                    val cleanAmt = tx.amount.coerceIn(-500.0, 500.0)
                                    val y = height * 0.54f - (cleanAmt.toFloat() / 500.0f) * (height * 0.35f)
                                    Offset(x, y)
                                }

                                // Build curved path
                                val path = Path()
                                points.reversed().forEachIndexed { i, pt ->
                                    if (i == 0) path.moveTo(pt.x, pt.y)
                                    else path.lineTo(pt.x, pt.y)
                                }

                                // Draw the outline wave
                                drawPath(
                                    path = path,
                                    color = if (isDarkMode) Color(0xFFBAC3FF) else Color(0xFF0D5EAF),
                                    style = Stroke(width = 4f)
                                )

                                // Area wave fill
                                val areaPath = Path().apply {
                                    addPath(path)
                                    lineTo(width, height)
                                    lineTo(0f, height)
                                    close()
                                }

                                drawPath(
                                    path = areaPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            (if (isDarkMode) Color(0xFFD1E4FF) else Color(0xFF0D5EAF)).copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )

                                // Dots indicator
                                points.forEach { pt ->
                                    drawCircle(
                                        color = if (isDarkMode) Color(0xFFD1E4FF) else Color(0xFF0D5EAF),
                                        radius = 5f,
                                        center = pt
                                    )
                                }
                            }
                        }
                    }

                    // Interactive Legend from "Sophisticated Dark"
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "OCIO" else "LEISURE",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.size(width = 30.dp, height = 3.dp).clip(CircleShape).background(if (isDarkMode) Color(0xFFBAC3FF) else Color(0xFF535F70)))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "HOGAR" else "HOME",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.size(width = 30.dp, height = 3.dp).clip(CircleShape).background(if (isDarkMode) Color(0xFF43474E) else Color.LightGray))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "INVERSIÓN" else "INVESTMENT",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.size(width = 30.dp, height = 3.dp).clip(CircleShape).background(if (isDarkMode) Color(0xFFD1E4FF) else Color(0xFF005AC1)))
                        }
                    }
                }
            }
        }

        // ANALISIS MICRO-GASTOS INDICATOR
        if (microSpends.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF1B2F3E) else Color(0xFFF0F7FF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.MoneyOff, null, tint = if (isDarkMode) Color(0xFF73C5FF) else Color(0xFF0B63A2), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                labels["section_micro"] ?: "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color(0xFF73C5FF) else Color(0xFF0B63A2)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "${labels["micro_title"] ?: ""}: ${microSpends.size}.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                        val totalMicroAmt = microSpends.sumOf { it.amount }
                        Text(
                            "${labels["micro_total_impact"] ?: ""}: ${"%.2f".format(totalMicroAmt)} EUR",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // SECCIÓN RECIENTES TRANSACTIONS
        item {
            Text(
                labels["recent_tx"] ?: "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color.Black
            )
        }

        if (txList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(labels["no_tx"] ?: "", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            items(txList) { tx ->
                TransactionListItem(tx = tx, isDarkMode = isDarkMode, viewModel = viewModel)
            }
        }
    }
}

// Inline connection pill items for Bank
@Composable
fun BankConnectItem(
    bankName: String,
    labels: Map<String, String>,
    avatarLetter: String,
    avatarBg: Color, // Can accept colors or styles
    isDarkMode: Boolean,
    isConnected: Boolean,
    balanceText: String? = null,
    onSync: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1A1C1E) else Color.White
        ),
        border = BorderStroke(1.dp, if (isConnected) (if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary) else (if (isDarkMode) Color(0xFF2D3135) else Color.Gray.copy(alpha = 0.2f))),
        modifier = Modifier
            .width(185.dp)
            .clickable { onSync() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular initials
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (avatarLetter == "R") Color(0xFF0052FF) else if (avatarLetter == "S") Color(0xFFEC0000) else Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        avatarLetter,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Small synchronization status dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) (if (isDarkMode) Color(0xFFBAC3FF) else Color(0xFF107C41)) else Color.Gray)
                )
            }

            Text(
                bankName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color.Black
            )

            if (isConnected && balanceText != null) {
                Text(
                    text = balanceText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkMode) Color(0xFFBAC3FF) else MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    if (isConnected) (labels["status_connected"] ?: "") else (labels["status_disconnected"] ?: ""),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Text(
                labels["sync_now"] ?: "",
                fontSize = 11.sp,
                color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Highly stylized list row item for transaction details
@Composable
fun TransactionListItem(tx: Transaction, isDarkMode: Boolean, viewModel: FinanceViewModel) {
    val selectCurrency by viewModel.selectedCurrency.collectAsState()
    val convertedAmount = viewModel.convertCurrency(tx.amount, tx.currency, selectCurrency)
    val showOriginal = tx.currency != selectCurrency

    val indicatorColor = if (tx.amount >= 0) (if (isDarkMode) Color(0xFFD1E4FF) else Color(0xFF107C41)) else (if (tx.isAnomaly) Color(0xFFD13438) else (if (isDarkMode) Color.White else Color.Black))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1A1C1E) else Color.White
        ),
        border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (tx.amount >= 0) {
                                (if (isDarkMode) Color(0xFFD1E4FF) else Color(0xFF107C41)).copy(alpha = 0.15f)
                            } else {
                                if (tx.isAnomaly) Color(0xFFD13438).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.amount >= 0) {
                            Icons.Filled.TrendingUp
                        } else {
                            if (tx.isAnomaly) Icons.Filled.ReportGmailerrorred else Icons.Filled.TrendingDown
                        },
                        contentDescription = null,
                        tint = if (tx.amount >= 0) {
                            if (isDarkMode) Color(0xFFD1E4FF) else Color(0xFF107C41)
                        } else {
                            if (tx.isAnomaly) Color(0xFFD13438) else Color.Gray
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            tx.concept,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (tx.isAnomaly) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFD13438))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("ANOMALY", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        "${tx.bankName} • ${tx.category}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (tx.amount > 0) "+" else ""}${viewModel.formatCurrency(convertedAmount, selectCurrency)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = indicatorColor
                )
                if (showOriginal) {
                    Text(
                        text = "(${if (tx.amount > 0) "+" else ""}${viewModel.formatCurrency(tx.amount, tx.currency)})",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

// --- TAB 2: AI COACH SCREEN ---
@Composable
fun CoachTab(
    viewModel: FinanceViewModel,
    labels: Map<String, String>,
    isDarkMode: Boolean
) {
    val responseText by viewModel.coachingResponse.collectAsState()
    val isLoading by viewModel.isCoachingLoading.collectAsState()
    val selectCurrency by viewModel.selectedCurrency.collectAsState()
    val currentLang by viewModel.language.collectAsState()
    val isSpan = currentLang == "es"
    
    val txState by viewModel.transactions.collectAsState()

    val beerAndLeisureTotal = remember(txState, selectCurrency) {
        txState.filter { tx ->
            tx.amount < 0 && (
                tx.concept.contains("cerveza", ignoreCase = true) ||
                tx.concept.contains("bar", ignoreCase = true) ||
                tx.concept.contains("pub", ignoreCase = true) ||
                tx.category.contains("Ocio", ignoreCase = true) ||
                tx.category.contains("Restaurante", ignoreCase = true)
            )
        }.sumOf { viewModel.convertCurrency(-it.amount, it.currency, selectCurrency) }
    }

    val subscriptionTotal = remember(txState, selectCurrency) {
        txState.filter { tx ->
            tx.amount < 0 && tx.category.contains("Suscripci", ignoreCase = true)
        }.sumOf { viewModel.convertCurrency(-it.amount, it.currency, selectCurrency) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // AI Advisor Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) Color(0xFF1B2B41) else Color(0xFFE5F1FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = if (isDarkMode) Color(0xFF73C5FF) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isSpan) "Smart Insights" else "Smart Insights",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black
                )
                Text(
                    text = if (isSpan) "Oportunidades y patrones detectados" else "Opportunities & detected patterns",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        // Section: Recommended Bank Offers (Visual Horizontal Scroll)
        Column {
            Text(
                text = if (isSpan) "Recomendaciones para ti" else "Recommended for you",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Feature Card 1: ING
                OfferCard(
                    title = if (isSpan) "ING: Llévate 200€" else "ING: Get 200€",
                    subtitle = if (isSpan) "Domicilia tu nómina y consigue 200€ gratis en tu Cuenta Nómina." else "Direct deposit your salary and get a 200€ cash bonus.",
                    icon = Icons.Filled.AccountBalance,
                    iconTint = Color(0xFFFF6600),
                    isDarkMode = isDarkMode,
                    actionText = if (isSpan) "Ver oferta" else "View offer"
                )

                // Feature Card 2: BBVA
                OfferCard(
                    title = if (isSpan) "BBVA: 750€ en 6 meses" else "BBVA: 750€ in 6 months",
                    subtitle = if (isSpan) "Plan Amigo Nómina: trae tus ingresos y recibe recompensas mensuales." else "Bring your monthly income and receive monthly cashback rewards.",
                    icon = Icons.Filled.CardGiftcard,
                    iconTint = Color(0xFF004481),
                    isDarkMode = isDarkMode,
                    actionText = if (isSpan) "Descubrir" else "Discover"
                )

                // Feature Card 3: TradeRepublic
                OfferCard(
                    title = if (isSpan) "Trade Republic: 2% TAE" else "Trade Republic: 2% APR",
                    subtitle = if (isSpan) "Haz crecer tus ahorros inactivos con interés mensual garantizado." else "Grow your idle savings with guaranteed monthly interest.",
                    icon = Icons.Filled.TrendingUp,
                    iconTint = Color(0xFF107C41),
                    isDarkMode = isDarkMode,
                    actionText = if (isSpan) "Invertir" else "Invest"
                )
            }
        }

        // Section: Visual Pattern Detection
        Column {
            Text(
                text = if (isSpan) "Patrones Detectados" else "Detected Patterns",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color.Black,
                modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
            )

            // Pattern Card 1: Leisure/Beer
            if (beerAndLeisureTotal > 0) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF4A1A1A) else Color(0xFFFFF0F0)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE22B43).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.LocalBar, contentDescription = null, tint = Color(0xFFE22B43))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isSpan) "Gasto elevado en Bar/Ocio" else "High spending in Leisure/Bars",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                            Text(
                                text = if (isSpan) "Has gastado ${viewModel.formatCurrency(beerAndLeisureTotal, selectCurrency)} este mes. Podrías ahorrar reduciendo salidas."
                                else "You've spent ${viewModel.formatCurrency(beerAndLeisureTotal, selectCurrency)} this month. Consider reducing outings to save.",
                                fontSize = 13.sp,
                                color = if (isDarkMode) Color(0xFFFFB4BC) else Color(0xFFC02A38),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Pattern Card 2: Subscriptions
            if (subscriptionTotal > 0) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF2C2513) else Color(0xFFFFF8E5)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFBB28).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Subscriptions, contentDescription = null, tint = Color(0xFFFFBB28))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isSpan) "Servicios Recurrentes" else "Recurring Services",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                            Text(
                                text = if (isSpan) "Tus suscripciones suman ${viewModel.formatCurrency(subscriptionTotal, selectCurrency)}. ¿Estás usando todos estos servicios?"
                                else "Your subscriptions sum up to ${viewModel.formatCurrency(subscriptionTotal, selectCurrency)}. Are you using all of them?",
                                fontSize = 13.sp,
                                color = if (isDarkMode) Color(0xFFFFD57B) else Color(0xFF9E7200),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
            
            // Empty pattern fallback if nothing matched
            if(beerAndLeisureTotal == 0.0 && subscriptionTotal == 0.0){
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF131619) else Color(0xFFF5F7FA)),
                    border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2A2E33) else Color(0xFFE2E8ED)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF00C49F), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (isSpan) "Tus gastos están bajo control. ¡Buen trabajo!" else "Your spendings are healthy. Great job!",
                            fontSize = 14.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        Divider(color = if (isDarkMode) Color(0xFF2D3135) else Color(0xFFECEFF1), modifier = Modifier.padding(vertical = 8.dp))

        // AI Deep Analysis Request (The prompt part)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF131619) else Color(0xFFF5F7FA)
            ),
            border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF23272B)) else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Psychology, contentDescription = null, tint = Color(0xFF8884D8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSpan) "Análisis Avanzado con IA" else "Deep AI Analysis",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                }

                Text(
                    text = if (isSpan) "Nuestra inteligencia artificial puede analizar todas tus transacciones a fondo y crear un informe ejecutivo personalizado."
                    else "Our AI can deeply analyze all your recent transactions to create a personalized executive report.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )

                // Action Button
                Button(
                    onClick = { viewModel.askCoachingAdvisor() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0xFF8884D8) else Color(0xFF673AB7),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("ask_ai_coach_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (isSpan) "Analizando..." else "Analyzing...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Filled.Insights, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSpan) "Generar Informe" else "Generate Report", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Response Rendering Area
        if (responseText.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Render paragraphs beautifully
                val paragraphs = responseText.split("\n\n").filter { it.isNotBlank() }
                paragraphs.forEachIndexed { index, para ->
                    val isHeading = para.length < 50 && (para.endsWith(":") || para.startsWith("#") || para.all { it.isUpperCase() || it.isWhitespace() })
                    val cleanText = para.replace(Regex("^#+\\s*"), "").replace("**", "")

                    if (isHeading) {
                        Text(
                            text = cleanText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color.Black,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    } else {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkMode) Color(0xFF15181A) else Color.White
                            ),
                            border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF202428)) else BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                val icon = when (index % 3) {
                                    0 -> Icons.Filled.LightbulbCircle
                                    1 -> Icons.Filled.TrendingUp
                                    else -> Icons.Filled.VerifiedUser
                                }
                                val iconColor = when (index % 3) {
                                    0 -> Color(0xFFFFBB28)
                                    1 -> Color(0xFF00C49F)
                                    else -> Color(0xFF0088FE)
                                }
                                
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(24.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = cleanText,
                                    fontSize = 14.sp,
                                    color = if (isDarkMode) Color(0xFFD0D5DD) else Color(0xFF475467),
                                    lineHeight = 22.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    textAlign = TextAlign.Justify
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OfferCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    isDarkMode: Boolean,
    actionText: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1C2025) else Color.White
        ),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2A2E33) else Color(0xFFE2E8ED)),
        modifier = Modifier.width(260.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black
                )
            }
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp,
                maxLines = 3
            )
            Button(
                onClick = { /* Simulated external link */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) Color(0xFF2C3139) else Color(0xFFF0F2F6),
                    contentColor = if (isDarkMode) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(36.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = actionText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- TAB 3: SAVINGS TARGETS ---
@Composable
fun GoalsTab(
    viewModel: FinanceViewModel,
    labels: Map<String, String>,
    isDarkMode: Boolean,
    onAddGoalClick: () -> Unit
) {
    val goalsList by viewModel.goals.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    labels["metas_title"] ?: "Goals & Milestone Savings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    labels["metas_desc"] ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        item {
            Button(
                onClick = onAddGoalClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_goal_trigger_button")
            ) {
                Icon(Icons.Filled.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(labels["add_goal"] ?: "Save Milestone Goal", fontWeight = FontWeight.Bold)
            }
        }

        if (goalsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay metas establecidas.", color = Color.Gray)
                }
            }
        } else {
            items(goalsList) { goal ->
                GoalListItem(goal = goal, isDarkMode = isDarkMode)
            }
        }
    }
}

@Composable
fun GoalListItem(goal: BudgetGoal, isDarkMode: Boolean) {
    val progress = (goal.savedAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1A1C1E) else Color.White
        ),
        border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        goal.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                    Text(
                        "${goal.category} • Vence el: ${goal.targetDate}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDarkMode) Color(0xFF1B2B41) else Color(0xFFECEFF1))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${"%.1f".format(progress * 100)}%",
                        color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar visualizer
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (isDarkMode) Color(0xFFBAC3FF) else MaterialTheme.colorScheme.primary,
                trackColor = if (isDarkMode) Color(0xFF2D3135) else Color(0xFFECEFF1)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Guardado: ${"%.2f".format(goal.savedAmount)} €",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    "Objetivo: ${"%.2f".format(goal.targetAmount)} €",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkMode) Color.White else Color.Black
                )
            }
        }
    }
}

// --- TAB 4: MANAGEMENT AND EXPORT HUB ---
@Composable
fun ManagementTab(
    viewModel: FinanceViewModel,
    labels: Map<String, String>,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isBiometricsActive by viewModel.isBiometricsEnabled.collectAsState()
    val checkedLang by viewModel.language.collectAsState()
    val selectCurrency by viewModel.selectedCurrency.collectAsState()
    val liveRates by viewModel.exchangeRatesState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            labels["config_security"] ?: "Configuration",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color.White else Color.Black
        )

        // EXPORT FILES HUB CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF161F30) else Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Módulo De Exportación E2EE" else "E2EE Secured Data Export",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black
                )

                Button(
                    onClick = {
                        val csv = viewModel.getCsvContent()
                        clipboardManager.setText(AnnotatedString(csv))
                        Toast.makeText(context, "${labels["export_csv"]}: ${labels["copied_clipboard"]}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("export_csv_btn")
                ) {
                    Icon(Icons.Filled.InsertDriveFile, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(labels["export_csv"] ?: "Export CSV")
                }

                Button(
                    onClick = {
                        val pdf = viewModel.getPdfReportContent()
                        clipboardManager.setText(AnnotatedString(pdf))
                        Toast.makeText(context, "${labels["export_pdf"]}: ${labels["copied_clipboard"]}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("export_pdf_btn")
                ) {
                    Icon(Icons.Filled.PictureAsPdf, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(labels["export_pdf"] ?: "Export PDF")
                }
            }
        }

        // SETTINGS TOGGLES LAYOUT
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1A1C1E) else Color.White
            ),
            border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Biometrics switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(labels["config_biometrics"] ?: "", color = if (isDarkMode) Color.White else Color.Black, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isBiometricsActive,
                        onCheckedChange = { viewModel.toggleBiometrics() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("biometric_toggle")
                    )
                }

                Divider(color = if (isDarkMode) Color(0xFF2D3135) else Color(0xFFE5E5E5))

                // Dark mode switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(labels["config_darkmode"] ?: "", color = if (isDarkMode) Color.White else Color.Black, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("dark_mode_toggle")
                    )
                }

                Divider(color = if (isDarkMode) Color(0xFF2D3135) else Color(0xFFE5E5E5))

                // Language Switcher Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(labels["config_language"] ?: "", color = if (isDarkMode) Color.White else Color.Black, fontWeight = FontWeight.Medium)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.setLanguage("es") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (checkedLang == "es") (if (isDarkMode) Color(0xFF3F4759) else Color(0xFFE3EDF7)) else Color.Transparent,
                                contentColor = if (checkedLang == "es") (if (isDarkMode) Color(0xFFD1E4FF) else Color.Black) else Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("lang_toggle_es")
                        ) {
                            Text("ESP 🇪🇸", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.setLanguage("en") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (checkedLang == "en") (if (isDarkMode) Color(0xFF3F4759) else Color(0xFFE3EDF7)) else Color.Transparent,
                                contentColor = if (checkedLang == "en") (if (isDarkMode) Color(0xFFD1E4FF) else Color.Black) else Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("lang_toggle_en")
                        ) {
                            Text("ENG 🇬🇧", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Divider(color = if (isDarkMode) Color(0xFF2D3135) else Color(0xFFE5E5E5))

                // Base Currency Switcher Toggle Row
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (checkedLang == "es") "Divisa Base Global" else "Global Base Currency",
                            color = if (isDarkMode) Color.White else Color.Black,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            listOf("EUR", "USD", "GBP", "JPY").forEach { curr ->
                                val label = when(curr) {
                                    "EUR" -> "EUR 🇪🇺"
                                    "USD" -> "USD 🇺🇸"
                                    "GBP" -> "GBP 🇬🇧"
                                    "JPY" -> "JPY 🇯🇵"
                                    else -> curr
                                }
                                Button(
                                    onClick = { viewModel.setBaseCurrency(curr) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectCurrency == curr) (if (isDarkMode) Color(0xFF3F4759) else Color(0xFFE3EDF7)) else Color.Transparent,
                                        contentColor = if (selectCurrency == curr) (if (isDarkMode) Color(0xFFD1E4FF) else Color.Black) else Color.Gray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp).testTag("currency_toggle_$curr")
                                ) {
                                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated live stock rates panel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDarkMode) Color(0x0AFFFFFF) else Color(0x05000000))
                            .border(1.dp, if (isDarkMode) Color(0x11FFFFFF) else Color(0x0A000000), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF107C41))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (checkedLang == "es") "MULTIACTIVOS EN TIEMPO REAL" else "REAL-TIME SYNCED ASSETS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF107C41)
                                    )
                                }
                                Text(
                                    text = "Base: 1 $selectCurrency",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("EUR", "USD", "GBP", "JPY").filter { it != selectCurrency }.forEach { alt ->
                                    val amountInAlt = viewModel.convertCurrency(1.0, selectCurrency, alt)
                                    val symbol = viewModel.getCurrencySymbol(alt)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(alt, fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            text = if (alt == "JPY") "${"%,.1f".format(amountInAlt)}$symbol" else "${"%,.3f".format(amountInAlt)}$symbol",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) Color.White else Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // DANGER ZONE CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF261217) else Color(0xFFFFF0F1)
            ),
            border = BorderStroke(1.dp, Color(0xFFE53935)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(labels["config_danger_zone"] ?: "", color = Color(0xFFE53935), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        viewModel.clearAllTransactions()
                        Toast.makeText(context, "Base de datos restablecida.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("danger_wipe_btn")
                ) {
                    Icon(Icons.Filled.DeleteForever, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(labels["config_delete_all"] ?: "Clear SQLite Database")
                }
            }
        }
    }
}

// --- FULL FORM MODALS (DIALOG POPUPS) ---

@Composable
fun AddTransactionModal(
    labels: Map<String, String>,
    onDismiss: () -> Unit,
    onConfirm: (concept: String, amount: Double, category: String, bank: String, currency: String) -> Unit
) {
    var concept by remember { mutableStateOf(TextFieldValue("")) }
    var amountStr by remember { mutableStateOf(TextFieldValue("")) }
    var selectedCategory by remember { mutableStateOf("Alimentos") }
    var selectedBank by remember { mutableStateOf("Manual") }
    var selectedCurrency by remember { mutableStateOf("EUR") }

    val categories = listOf("Alimentos", "Suscripción", "Transporte", "Restaurantes", "Ocio", "Inversiones", "Ingreso")
    val banks = listOf("Manual", "Revolut", "TradeRepublic", "Banco Tradicional")
    val currencies = listOf("EUR", "USD", "GBP", "JPY")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("add_trans_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(labels["add_tx"] ?: "Add Transaction", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text(labels["lbl_concept"] ?: "") },
                    modifier = Modifier.fillMaxWidth().testTag("input_tx_concept")
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(labels["lbl_amount"] ?: "") },
                    modifier = Modifier.fillMaxWidth().testTag("input_tx_amount")
                )

                // Currency selection
                Column {
                    Text(text = "Divisa / Currency", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currencies.forEach { cur ->
                            FilterChip(
                                selected = selectedCurrency == cur,
                                onClick = { selectedCurrency = cur },
                                label = { Text(cur) },
                                modifier = Modifier.testTag("chip_cur_$cur")
                            )
                        }
                    }
                }

                // Dropdown mock category selection using simple scrollable buttons row
                Column {
                    Text(labels["lbl_category"] ?: "", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                modifier = Modifier.testTag("chip_cat_$cat")
                            )
                        }
                    }
                }

                // Entity banks selection
                Column {
                    Text(labels["lbl_bank"] ?: "", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        banks.forEach { bn ->
                            FilterChip(
                                selected = selectedBank == bn,
                                onClick = { selectedBank = bn },
                                label = { Text(bn) },
                                modifier = Modifier.testTag("chip_bank_$bn")
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_tx_btn")) {
                        Text(labels["cancel"] ?: "Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.text.toDoubleOrNull() ?: -0.0
                            if (concept.text.isNotEmpty()) {
                                onConfirm(concept.text, amt, selectedCategory, selectedBank, selectedCurrency)
                            }
                        },
                        modifier = Modifier.testTag("submit_tx_btn")
                    ) {
                        Text(labels["btn_add"] ?: "Add")
                    }
                }
            }
        }
    }
}

@Composable
fun AddGoalModal(
    labels: Map<String, String>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, target: Double, date: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var targetStr by remember { mutableStateOf(TextFieldValue("")) }
    var category by remember { mutableStateOf("Retiro") }

    val categories = listOf("General", "Retiro", "Vacaciones", "Seguridad")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("add_goal_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(labels["add_goal"] ?: "Add Saving Goal", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(labels["lbl_goal_title"] ?: "") },
                    modifier = Modifier.fillMaxWidth().testTag("input_goal_title")
                )

                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text(labels["lbl_goal_target"] ?: "") },
                    modifier = Modifier.fillMaxWidth().testTag("input_goal_target")
                )

                Column {
                    Text(labels["lbl_category"] ?: "", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) },
                                modifier = Modifier.testTag("chip_goal_cat_$cat")
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_goal_btn")) {
                        Text(labels["cancel"] ?: "Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val tgt = targetStr.text.toDoubleOrNull() ?: 100.0
                            if (title.text.isNotEmpty()) {
                                onConfirm(title.text, tgt, "2026-12-31", category)
                            }
                        },
                        modifier = Modifier.testTag("submit_goal_btn")
                    ) {
                        Text(labels["add_goal"] ?: "Save")
                    }
                }
            }
        }
    }
}

// =========================================================================
// --- HISTORY TAB & CUSTOM AUTOMATED CATEGORIZATION / SMART LEARNING ---
// =========================================================================

@Composable
fun HistoryTab(viewModel: FinanceViewModel, labels: Map<String, String>, isDarkMode: Boolean) {
    val txState by viewModel.transactions.collectAsState()
    val categoryItems by viewModel.categoryItems.collectAsState()
    val rules by viewModel.rules.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedBankFilter by remember { mutableStateOf("Todos") }
    var selectedCategoryFilter by remember { mutableStateOf("Todos") }
    var selectedTypeFilter by remember { mutableStateOf("Todos") } // "Todos", "Gastos", "Ingresos"

    var showManageCategoriesDialog by remember { mutableStateOf(false) }
    var txToEditCategory by remember { mutableStateOf<Transaction?>(null) }

    // Filter transaction list sorted by timestamp DESC
    val filteredTx = remember(txState, searchQuery, selectedBankFilter, selectedCategoryFilter, selectedTypeFilter) {
        txState.filter { tx ->
            val matchesSearch = tx.concept.contains(searchQuery, ignoreCase = true) ||
                    tx.category.contains(searchQuery, ignoreCase = true) ||
                    tx.subcategory.contains(searchQuery, ignoreCase = true)
            
            val matchesBank = selectedBankFilter == "Todos" || tx.bankName == selectedBankFilter
            val matchesCat = selectedCategoryFilter == "Todos" || tx.category == selectedCategoryFilter
            val matchesType = when (selectedTypeFilter) {
                "Gastos" -> tx.amount < 0
                "Ingresos" -> tx.amount > 0
                else -> true
            }

            matchesSearch && matchesBank && matchesCat && matchesType
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Header
        Text(
            text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Historial de Transacciones" else "Transaction History",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
        )

        // Primary Interactive Filter Buttons Row (Segmented style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDarkMode) Color(0xFF141618) else Color(0xFFECEFF1))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Todos", "Gastos", "Ingresos").forEach { type ->
                val isSelected = selectedTypeFilter == type
                val labelText = when (type) {
                    "Todos" -> if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Todo" else "All"
                    "Gastos" -> if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Gastos" else "Expenses"
                    "Ingresos" -> if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Ingresos" else "Income"
                    else -> type
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) {
                                if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.primaryContainer
                            } else Color.Transparent
                        )
                        .clickable { selectedTypeFilter = type }
                        .padding(vertical = 10.dp)
                        .testTag("history_type_btn_$type"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelText,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) {
                            if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.onPrimaryContainer
                        } else Color.Gray
                    )
                }
            }
        }

        // Action Options ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showManageCategoriesDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Category, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Administrar Categorías" else "Manage Categories", fontSize = 12.sp)
            }
            
            // Show count of rules learned
            Text(
                text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "IA Aprendiendo: ${rules.size} reglas" else "AI Learning: ${rules.size} rules",
                fontSize = 11.sp,
                color = Color.Gray,
                fontStyle = FontStyle.Italic
            )
        }

        // Search & Filters Box
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF141618) else Color(0xFFF0F2F6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Buscar por concepto, categoría..." else "Search concept or categories...") },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Filter by Entity (BankName)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Filtrar por Entidad:" else "Filter by Entity:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Todos", "Revolut", "TradeRepublic", "Banco Tradicional", "Manual").forEach { bank ->
                            val isSelected = selectedBankFilter == bank
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedBankFilter = bank },
                                label = { Text(bank, fontSize = 11.sp) },
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                }

                // Filter by Category
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Filtrar por Categoría:" else "Filter by Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val distinctCats = remember(categoryItems) {
                            listOf("Todos") + categoryItems.map { it.category }.distinct()
                        }
                        distinctCats.forEach { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                }

                }
            }

        // Transactions List View
        if (filteredTx.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "No se encontraron transacciones." else "No transactions match your search.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredTx.forEach { tx ->
                    TransactionHistoryItem(
                        tx = tx,
                        isDarkMode = isDarkMode,
                        labels = labels,
                        viewModel = viewModel,
                        onEditClick = { txToEditCategory = tx }
                    )
                }
            }
        }
    }

    // --- MANAGE CATEGORIES OVERLAY POPUP ---
    if (showManageCategoriesDialog) {
        ManageCategoriesModal(
            viewModel = viewModel,
            labels = labels,
            isDarkMode = isDarkMode,
            onDismiss = { showManageCategoriesDialog = false }
        )
    }

    // --- EDIT CATEGORIZATION DIALOG LINKED TO IA LEARNING ---
    if (txToEditCategory != null) {
        EditTransactionCategorizationModal(
            transaction = txToEditCategory!!,
            viewModel = viewModel,
            labels = labels,
            isDarkMode = isDarkMode,
            onDismiss = { txToEditCategory = null }
        )
    }
}

@Composable
fun TransactionHistoryItem(
    tx: Transaction,
    isDarkMode: Boolean,
    labels: Map<String, String>,
    viewModel: FinanceViewModel,
    onEditClick: () -> Unit
) {
    val selectCurrency by viewModel.selectedCurrency.collectAsState()
    val isExpense = tx.amount < 0
    val amountColor = if (isExpense) {
        if (isDarkMode) Color(0xFFBAC3FF) else Color(0xFFBA1A1A)
    } else {
        if (isDarkMode) Color(0xFFB4F1B6) else Color(0xFF006E1B)
    }
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateStr = formatter.format(Date(tx.timestamp))

    // Real-time currency conversion
    val convertedAmount = viewModel.convertCurrency(tx.amount, tx.currency, selectCurrency)
    val formattedAmount = viewModel.formatCurrency(convertedAmount, selectCurrency)
    val showOriginalSymbol = tx.currency != selectCurrency

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E2125) else Color.White
        ),
        border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = tx.concept,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                    Text(text = dateStr, fontSize = 11.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (!isExpense && convertedAmount > 0) "+" else ""}$formattedAmount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = amountColor
                    )
                    if (showOriginalSymbol) {
                        Text(
                            text = "(${if (!isExpense && tx.amount > 0) "+" else ""}${viewModel.formatCurrency(tx.amount, tx.currency)})",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Autonomous Category Tags
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDarkMode) Color(0xFF1B2B41) else Color(0xFFE5F1FD))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tx.category.uppercase(),
                            color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (tx.subcategory.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isDarkMode) Color(0xFF232D28) else Color(0xFFE6F5EA))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tx.subcategory.uppercase(),
                                color = if (isDarkMode) Color(0xFFB4F1B6) else Color(0xFF006E1B),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bank Identifier Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDarkMode) Color(0xFF28282B) else Color(0xFFECEFF1))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(text = tx.bankName, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }

                // Edit/Classify Trigger Icon
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit categorization",
                        tint = if (isDarkMode) Color(0xFFBAC3FF) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (tx.isAnomaly) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDarkMode) Color(0xFF351C1C) else Color(0xFFFEE8E8))
                        .padding(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, tint = Color.Red, modifier = Modifier.size(12.dp))
                        Text(tx.anomalyReason ?: "Anomaly detected", color = if (isDarkMode) Color(0xFFFFB4AB) else Color.Red, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EditTransactionCategorizationModal(
    transaction: Transaction,
    viewModel: FinanceViewModel,
    labels: Map<String, String>,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    val categoryItems by viewModel.categoryItems.collectAsState()
    
    // Set initial values
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var selectedSubcategory by remember { mutableStateOf(transaction.subcategory) }

    // Distinct list of categories
    val categories = remember(categoryItems) {
        categoryItems.map { it.category }.distinct().ifEmpty { listOf("Alimentos", "Suscripción", "Transporte", "Restaurantes", "Ocio", "Salud", "Ingreso", "Inversiones", "Manual") }
    }

    // Filter subcategories matching selected Category
    val availableSubcategories = remember(categoryItems, selectedCategory) {
        categoryItems.filter { it.category == selectedCategory }.map { it.subcategory }.distinct().ifEmpty { listOf("Otros") }
    }

    LaunchedEffect(selectedCategory) {
        if (selectedCategory != transaction.category) {
            selectedSubcategory = availableSubcategories.firstOrNull() ?: "Otros"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("edit_categorization_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Editar Categorización" else "Edit Categorization",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "\"${transaction.concept}\"",
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
                )

                // Select Category Dropdown Button Row
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Categoría Principal:" else "Category:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                }

                // Select Subcategory Dropdown Button Row
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Subcategoría:" else "Subcategory:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableSubcategories.forEach { sub ->
                            val isSelected = selectedSubcategory == sub
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSubcategory = sub },
                                label = { Text(sub, fontSize = 11.sp) },
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                }

                // UI Learning Cue Highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDarkMode) Color(0xFF232A35) else Color(0xFFE5F1FD))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Psychology,
                                contentDescription = null,
                                tint = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "FinSage Aprendizaje IA" else "FinSage AI Learning",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") {
                                "Al guardar, la aplicación aprenderá autónomamente de este ajuste. Todas tus transacciones históricas o futuras que contengan \"${transaction.concept}\" se clasificarán automáticamente como $selectedCategory > $selectedSubcategory."
                            } else {
                                "Once saved, FinSage learns from your action. All future & historic transactions containing \"${transaction.concept}\" will automatically map to $selectedCategory > $selectedSubcategory."
                            },
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(labels["cancel"] ?: "Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            viewModel.updateTransactionCategory(transaction, selectedCategory, selectedSubcategory)
                            onDismiss()
                        }
                    ) {
                        Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Guardar y Enseñar" else "Save & Train")
                    }
                }
            }
        }
    }
}

@Composable
fun ManageCategoriesModal(
    viewModel: FinanceViewModel,
    labels: Map<String, String>,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    val categoryItems by viewModel.categoryItems.collectAsState()

    var newCategory by remember { mutableStateOf(TextFieldValue("")) }
    var newSubcategory by remember { mutableStateOf(TextFieldValue("")) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(8.dp)
                .testTag("manage_categories_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Gestionar Categorías" else "Manage Categories",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") {
                        "Añada o elimine combinaciones de categoría y subcategoría para transacciones."
                    } else {
                        "Add or remove custom categories and subcategories that you want to classify transactions with."
                    },
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // Input form to add a combination
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF131517) else Color(0xFFF0F2F6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Añadir Nueva Categorización" else "Add New Category Mapping", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newCategory,
                                onValueChange = { newCategory = it },
                                placeholder = { Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Categoría" else "Category") },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 11.sp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newSubcategory,
                                onValueChange = { newSubcategory = it },
                                placeholder = { Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Subcateg." else "Subcategory") },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 11.sp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(
                            onClick = {
                                if (newCategory.text.trim().isNotEmpty() && newSubcategory.text.trim().isNotEmpty()) {
                                    viewModel.addCustomCategoryItem(newCategory.text, newSubcategory.text)
                                    newCategory = TextFieldValue("")
                                    newSubcategory = TextFieldValue("")
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Crear Categoría" else "Create Category", fontSize = 12.sp)
                        }
                    }
                }

                // Scrollable List of existing items
                Text(
                    text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Listado de Categorías de Transacción" else "Current Mappings",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categoryItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDarkMode) Color(0xFF22252A) else Color(0xFFF9F9FB))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.category, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(item.subcategory, fontSize = 11.sp, color = Color.Gray)
                            }
                            IconButton(
                                onClick = { viewModel.deleteCustomCategoryItem(item) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Cerrar" else "Close")
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    isDarkMode: Boolean
) {
    val bg = if (selected) {
        if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.primaryContainer
    } else {
        if (isDarkMode) Color(0xFF1E2125) else Color(0xFFECEFF1)
    }
    val contentColor = if (selected) {
        if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        if (isDarkMode) Color.Gray else Color.DarkGray
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(
                width = 1.dp,
                color = if (selected) (if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            label()
        }
    }
}

// =========================================================================
// --- CHARTS & ADVANCED INTERACTIVE ANALYTICS TAB (SOPHISTICATED DARK) ---
// =========================================================================

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChartsTab(
    viewModel: FinanceViewModel,
    labels: Map<String, String>,
    isDarkMode: Boolean
) {
    val txState by viewModel.transactions.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val selectCurrency by viewModel.selectedCurrency.collectAsState()
    val checkedLang by viewModel.language.collectAsState()

    val isSpanish = checkedLang == "es"
    var selectedSection by remember { mutableStateOf("desglose") } // "desglose", "metas", "tendencias"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Header
        Text(
            text = if (isSpanish) "Análisis e Interactividad" else "Interactive Analytics",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
        )

        // Custom Top Tabs Segmented Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDarkMode) Color(0xFF141618) else Color(0xFFECEFF1))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("desglose", "metas", "tendencias").forEach { section ->
                val isSelected = selectedSection == section
                val labelText = when (section) {
                    "desglose" -> if (isSpanish) "Gasto" else "Expenses"
                    "metas" -> if (isSpanish) "Metas" else "Goals"
                    "tendencias" -> if (isSpanish) "Tendencias" else "Trends"
                    else -> section
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) {
                                if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.primaryContainer
                            } else Color.Transparent
                        )
                        .clickable { selectedSection = section }
                        .padding(vertical = 10.dp)
                        .testTag("chart_tab_$section"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelText,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) {
                            if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.onPrimaryContainer
                        } else Color.Gray
                    )
                }
            }
        }

        // Animated render
        AnimatedContent(
            targetState = selectedSection,
            transitionSpec = {
                fadeIn(animationSpec = tween(250)) with fadeOut(animationSpec = tween(200))
            },
            modifier = Modifier.fillMaxWidth()
        ) { target ->
            when (target) {
                "desglose" -> {
                    ExpenseBreakdownSection(
                        txState = txState,
                        viewModel = viewModel,
                        isDarkMode = isDarkMode,
                        selectCurrency = selectCurrency,
                        isSpanish = isSpanish
                    )
                }
                "metas" -> {
                    GoalsAnalysisSection(
                        goals = goals,
                        txState = txState,
                        viewModel = viewModel,
                        isDarkMode = isDarkMode,
                        selectCurrency = selectCurrency,
                        isSpanish = isSpanish
                    )
                }
                "tendencias" -> {
                    TrendLineSection(
                        txState = txState,
                        viewModel = viewModel,
                        isDarkMode = isDarkMode,
                        selectCurrency = selectCurrency,
                        isSpanish = isSpanish
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 1. EXPENSE BREAKDOWN (DONUT PIE CHART + TAP DETECTOR + BUDGET SLIDER)
// -------------------------------------------------------------------------

@Composable
fun ExpenseBreakdownSection(
    txState: List<Transaction>,
    viewModel: FinanceViewModel,
    isDarkMode: Boolean,
    selectCurrency: String,
    isSpanish: Boolean
) {
    val expenses = remember(txState) { txState.filter { it.amount < 0 } }

    val spendingList = remember(expenses, selectCurrency) {
        expenses.groupBy { it.category }
            .mapValues { entry ->
                entry.value.sumOf { tx ->
                    viewModel.convertCurrency(-tx.amount, tx.currency, selectCurrency)
                }
            }
            .toList()
            .sortedByDescending { it.second }
    }

    val totalSpend = remember(spendingList) { spendingList.sumOf { it.second } }

    var selectedIdx by remember { mutableStateOf(-1) }
    var reductionPercent by remember { mutableStateOf(30f) }

    val colorsPalette = listOf(
        Color(0xFF0088FE), // Ocean Blue
        Color(0xFF00C49F), // Teal
        Color(0xFFFFBB28), // Golden Yellow
        Color(0xFFFF8042), // Coral Coral
        Color(0xFF8884D8), // Pastel Purple
        Color(0xFF82CA9D), // Light Emerald
        Color(0xFF00E4A0), // Forest Mint
        Color(0xFFBAC3FF)  // Soft Indigo
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E2125) else Color.White
        ),
        border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isSpanish) "Exploración de Ecosistemas de Gasto" else "Expense Ecosystem Explorer",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color.Black,
                modifier = Modifier.align(Alignment.Start)
            )

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSpanish) "Sin gastos registrados aún" else "No spending logged yet",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Custom Donut Canvas Draft
                    val density = LocalDensity.current
                    val canvasSizeDp = 220.dp
                    val canvasSizePx = with(density) { canvasSizeDp.toPx() }

                    Canvas(
                        modifier = Modifier
                            .size(canvasSizeDp)
                            .testTag("donut_spending_canvas")
                            .pointerInput(spendingList) {
                                detectTapGestures { offset ->
                                    val cx = canvasSizePx / 2f
                                    val cy = canvasSizePx / 2f
                                    val dx = offset.x - cx
                                    val dy = offset.y - cy
                                    val dist = Math.hypot(dx.toDouble(), dy.toDouble())

                                    val outerRadius = (canvasSizePx / 2f).toDouble()
                                    val innerRadius = outerRadius * 0.45

                                    if (dist in innerRadius..outerRadius) {
                                        var angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble()))
                                        if (angle < 0) angle += 360.0

                                        var currentAngle = 0.0
                                        var clickedIdx = -1
                                        spendingList.forEachIndexed { i, pair ->
                                            val percent = pair.second / totalSpend
                                            val sweep = percent * 360.0
                                            if (angle >= currentAngle && angle < currentAngle + sweep) {
                                                clickedIdx = i
                                            }
                                            currentAngle += sweep
                                        }
                                        if (clickedIdx != -1) {
                                            selectedIdx = if (selectedIdx == clickedIdx) -1 else clickedIdx
                                        }
                                    } else {
                                        selectedIdx = -1
                                    }
                                }
                            }
                    ) {
                        var currentAngle = 0f
                        spendingList.forEachIndexed { idx, pair ->
                            val percent = pair.second / totalSpend
                            val sweepAngle = percent * 360f
                            val isSelected = (selectedIdx == idx)
                            val strokeWidth = if (isSelected) 36.dp.toPx() else 22.dp.toPx()
                            val color = colorsPalette[idx % colorsPalette.size]

                            drawArc(
                                color = color,
                                startAngle = currentAngle,
                                sweepAngle = sweepAngle.toFloat(),
                                useCenter = false,
                                style = Stroke(width = strokeWidth),
                                size = Size(size.width, size.height)
                            )
                            currentAngle += sweepAngle.toFloat()
                        }
                    }

                    // Inside donut hole display info
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isSpanish) "Gasto Total" else "Total Spend",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = viewModel.formatCurrency(totalSpend, selectCurrency),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    }
                }

                // Interactive Legend Rows
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    spendingList.forEachIndexed { idx, pair ->
                        val isSelected = (selectedIdx == idx)
                        val color = colorsPalette[idx % colorsPalette.size]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) (if (isDarkMode) Color(0xFF2C2F34) else Color(0xFFE3EDF7)) else Color.Transparent)
                                .clickable { selectedIdx = if (selectedIdx == idx) -1 else idx }
                                .padding(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = pair.first,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                        }
                    }
                }

                Divider(color = if (isDarkMode) Color(0xFF2D3135) else Color(0xFFECEFF1))

                // Interactive Simulator Card
                val highlightedItem = spendingList.getOrNull(selectedIdx)
                if (highlightedItem != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = (if (isSpanish) "Simulador de Ajuste: " else "Smart adjustment simulator: ") + highlightedItem.first,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorsPalette[selectedIdx % colorsPalette.size]
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSpanish) "Reducción de gasto:" else "Reduce budget spent:",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${reductionPercent.toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                        }

                        Slider(
                            value = reductionPercent,
                            onValueChange = { reductionPercent = it },
                            valueRange = 0f..100f,
                            modifier = Modifier.fillMaxWidth().testTag("reduction_slider")
                        )

                        val rawSavingMonthly = highlightedItem.second * (reductionPercent / 100)
                        val rawSavingYearly = rawSavingMonthly * 12

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDarkMode) Color(0x1A00C49F) else Color(0x0A00C49F))
                                .border(1.dp, Color(0xFF00C49F).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isSpanish) "⚡ IMPACTO AL AHORRO GENERADO" else "⚡ SIMULATED SAVINGS IMPACT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00C49F)
                                )
                                Text(
                                    text = if (isSpanish) {
                                        "Ajustando un ${reductionPercent.toInt()}% tu presupuesto en ${highlightedItem.first}, retendrás ${viewModel.formatCurrency(rawSavingMonthly, selectCurrency)} adicionales de forma mensual (o ${viewModel.formatCurrency(rawSavingYearly, selectCurrency)} al año)."
                                    } else {
                                        "By cutting your ${highlightedItem.first} outflow by ${reductionPercent.toInt()}% you save ${viewModel.formatCurrency(rawSavingMonthly, selectCurrency)} per month (${viewModel.formatCurrency(rawSavingYearly, selectCurrency)} per year)."
                                    },
                                    fontSize = 11.sp,
                                    color = if (isDarkMode) Color.White else Color.DarkGray
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSpanish) "💡 Toca cualquier porción de la rueda para simular un plan de ahorro" else "💡 Tap on any slice above to test a budget adjustment plan",
                            style = TextStyle(fontStyle = FontStyle.Italic),
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 2. GOALS PROGRESS SECTION (VERTICAL BARS + ACCELERATOR SIMULATOR)
// -------------------------------------------------------------------------

@Composable
fun GoalsAnalysisSection(
    goals: List<BudgetGoal>,
    txState: List<Transaction>,
    viewModel: FinanceViewModel,
    isDarkMode: Boolean,
    selectCurrency: String,
    isSpanish: Boolean
) {
    if (goals.isEmpty()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E2125) else Color.White),
            border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSpanish) "Añade una meta en la pestaña 'Metas' para comenzar el modelado analítico." else "Create a goal in 'Goals' to unlock analytical projections.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    var selectedGoalIdx by remember { mutableStateOf(0) }
    var variableSavingsCutPercent by remember { mutableStateOf(20f) }

    val activeGoal = goals.getOrNull(selectedGoalIdx) ?: goals.first()

    // Variable expenses: sum of food, leisure, restaurants in past 30 days
    val monthlyVariableSpent = remember(txState, selectCurrency) {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        txState.filter {
            it.timestamp >= thirtyDaysAgo && it.amount < 0 &&
                    (it.category == "Ocio" || it.category == "Restaurantes" || it.category == "Alimentos" || it.category == "Suscripción" ||
                     it.category == "Leisure" || it.category == "Restaurants" || it.category == "Food")
        }.sumOf { tx ->
            viewModel.convertCurrency(-tx.amount, tx.currency, selectCurrency)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E2125) else Color.White),
        border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (isSpanish) "Progreso y Simulación de Metas" else "Progress & Savings Accelerator",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color.Black
            )

            // Custom Interactive Multi-goal Bar Graphic
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                goals.forEachIndexed { i, g ->
                    val isSelected = (selectedGoalIdx == i)
                    val completion = if (g.targetAmount > 0) (g.savedAmount / g.targetAmount).toFloat().coerceIn(0f, 1f) else 0f

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) (if (isDarkMode) Color(0xFF2C2F34) else Color(0xFFE3EDF7)) else Color.Transparent)
                            .clickable { selectedGoalIdx = i }
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = g.title + (if (isSelected) " 🎯" else ""),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                            Text(
                                text = "${(completion * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (completion >= 1f) Color(0xFF00C49F) else Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Overlapping custom linear indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(if (isDarkMode) Color(0xFF2A2A2F) else Color(0xFFECEFF1))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(completion)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = if (isSelected) {
                                                listOf(Color(0xFF8884D8), Color(0xFF82CA9D))
                                            } else {
                                                listOf(Color.Gray.copy(alpha = 0.5f), Color.Gray)
                                            }
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            Divider(color = if (isDarkMode) Color(0xFF2D3135) else Color(0xFFECEFF1))

            // Savings Accelerator Simulator
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isSpanish) "⚡ Acelerador de Metas Neural" else "⚡ Neural Savings Booster",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8884D8)
                )

                Text(
                    text = if (isSpanish) {
                        "Ajustemos tus gastos secundarios mensuales de entretenimiento y comida (Actual: ${viewModel.formatCurrency(monthlyVariableSpent, selectCurrency)} en 30 días)."
                    } else {
                        "Let's simulate scaling back food and luxury spends (Variable total: ${viewModel.formatCurrency(monthlyVariableSpent, selectCurrency)} past 30 days)."
                    },
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSpanish) "Nivel de recorte simulado:" else "Simulated haircut level:",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${variableSavingsCutPercent.toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                }

                Slider(
                    value = variableSavingsCutPercent,
                    onValueChange = { variableSavingsCutPercent = it },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth().testTag("goal_boost_slider")
                )

                val monthlySavingsBoost = monthlyVariableSpent * (variableSavingsCutPercent / 100.0)
                val remainingAmount = (activeGoal.targetAmount - activeGoal.savedAmount).coerceAtLeast(0.0)

                // Suppose standard daily saving rate is 2 EUR, baseline monthly is 60 EUR.
                val baseMonthlyRate = 60.0
                val baselineDaily = baseMonthlyRate / 30.0
                val acceleratedDaily = (baseMonthlyRate + monthlySavingsBoost) / 30.0

                val defaultDaysRemaining = remainingAmount / baselineDaily
                val acceleratedDaysRemaining = remainingAmount / acceleratedDaily
                val daysSaved = (defaultDaysRemaining - acceleratedDaysRemaining).toInt().coerceAtLeast(0)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkMode) Color(0x1F8884D8) else Color(0x0F8884D8))
                        .border(1.dp, Color(0xFF8884D8).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isSpanish) "📊 IMPACTO EN EL LOGRO" else "📊 MILESTONE SPEED IMPACT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8884D8)
                        )
                        if (remainingAmount <= 0) {
                            Text(
                                text = if (isSpanish) "¡Felicidades! Esta meta de ahorro ya se ha alcanzado." else "Congratulations! Your goals target has already been reached.",
                                fontSize = 11.sp,
                                color = if (isDarkMode) Color.White else Color.DarkGray
                            )
                        } else {
                            Text(
                                text = if (isSpanish) {
                                    "Incrementas tu tasa de ahorro en ${viewModel.formatCurrency(monthlySavingsBoost, selectCurrency)} adicionales al mes. ¡Alcanzarás tu meta '${activeGoal.title}' $daysSaved días antes de lo proyectado!"
                                } else {
                                    "Boosting monthly savings by ${viewModel.formatCurrency(monthlySavingsBoost, selectCurrency)}. You will hit your '${activeGoal.title}' target $daysSaved days sooner!"
                                },
                                fontSize = 11.sp,
                                color = if (isDarkMode) Color.White else Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 3. RETROSPECTIVE TREND LINE PLOT (LINE CHART + INTERACTIVE DRAG HOVER)
// -------------------------------------------------------------------------

@Composable
fun TrendLineSection(
    txState: List<Transaction>,
    viewModel: FinanceViewModel,
    isDarkMode: Boolean,
    selectCurrency: String,
    isSpanish: Boolean
) {
    val chronologicalTx = remember(txState) { txState.sortedBy { it.timestamp } }

    val runningBalances = remember(chronologicalTx, selectCurrency) {
        var base = 2500.0 // healthy base line
        chronologicalTx.map { tx ->
            val amtConverted = viewModel.convertCurrency(tx.amount, tx.currency, selectCurrency)
            base += amtConverted
            Pair(tx, base)
        }
    }

    if (runningBalances.isEmpty()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E2125) else Color.White),
            border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSpanish) "Sin suficientes datos para generar tendencia balanceal." else "No historical balance stats to trace. Add some cash flows first.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        return
    }

    var hoveredIdx by remember { mutableStateOf(-1) }

    val maxVal = remember(runningBalances) { (runningBalances.maxOf { it.second } * 1.1) }
    val minVal = remember(runningBalances) { (runningBalances.minOf { it.second } * 0.9).coerceAtLeast(0.0) }
    val valueRange = (maxVal - minVal).coerceAtLeast(1.0)

    val activeHover = runningBalances.getOrNull(hoveredIdx)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E2125) else Color.White),
        border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isSpanish) "Evolución Balanceal Temporal" else "Historical Net Worth Trend",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                    Text(
                        text = if (isSpanish) "Arrastra el dedo para explorar puntos" else "Drag finger along chart to inspect values",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                if (activeHover != null) {
                    Text(
                        text = viewModel.formatCurrency(activeHover.second, selectCurrency),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // HUD details representation
            if (activeHover != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDarkMode) Color(0xFF2C2F34) else Color(0xFFF0F2F6))
                        .padding(8.dp)
                ) {
                    val dateFormatted = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(activeHover.first.timestamp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(activeHover.first.concept, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color.Black)
                            Text(dateFormatted, fontSize = 9.sp, color = Color.Gray)
                        }
                        val changeAmt = viewModel.convertCurrency(activeHover.first.amount, activeHover.first.currency, selectCurrency)
                        Text(
                            text = (if (changeAmt >= 0) "+" else "") + viewModel.formatCurrency(changeAmt, selectCurrency),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (changeAmt < 0) Color(0xFFE22B43) else Color(0xFF107C41)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(34.dp))
            }

            // Render custom line and area plot inside Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val density = LocalDensity.current
                val heightPx = with(density) { 200.dp.toPx() }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("trend_line_canvas")
                        .pointerInput(runningBalances) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val itemWidth = size.width / runningBalances.size.coerceAtLeast(1)
                                    val idx = (offset.x / itemWidth).toInt().coerceIn(0, runningBalances.lastIndex)
                                    hoveredIdx = idx
                                },
                                onDrag = { change, _ ->
                                    val itemWidth = size.width / runningBalances.size.coerceAtLeast(1)
                                    val idx = (change.position.x / itemWidth).toInt().coerceIn(0, runningBalances.lastIndex)
                                    hoveredIdx = idx
                                },
                                onDragEnd = {
                                    hoveredIdx = -1
                                }
                            )
                        }
                ) {
                    val cWidth = size.width
                    val cHeight = size.height
                    val itemWidth = cWidth / runningBalances.size.coerceAtLeast(1)

                    val points = runningBalances.mapIndexed { i, p ->
                        val x = i * itemWidth
                        val ratio = (p.second - minVal) / valueRange
                        val y = cHeight - (ratio * cHeight).toFloat()
                        Offset(x, y)
                    }

                    // Draw area under line curve
                    if (points.isNotEmpty()) {
                        val areaPath = Path().apply {
                            moveTo(0f, cHeight)
                            points.forEach { pt ->
                                lineTo(pt.x, pt.y)
                            }
                            lineTo(points.last().x, cHeight)
                            close()
                        }
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0x3B00C49F), Color.Transparent)
                            )
                        )

                        // Draw line stroke
                        val linePath = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1..points.lastIndex) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = Color(0xFF00C49F),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw tooltip vertical guides on hover
                        if (hoveredIdx != -1 && hoveredIdx < points.size) {
                            val activePt = points[hoveredIdx]
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.5f),
                                start = Offset(activePt.x, 0f),
                                end = Offset(activePt.x, cHeight),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawCircle(
                                color = Color(0xFF00C49F),
                                radius = 6.dp.toPx(),
                                center = activePt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = activePt
                            )
                        }
                    }
                }
            }
        }
    }
}


