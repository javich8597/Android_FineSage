package com.example.ui.shared

import android.content.Context
import android.widget.Toast
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.finance.FinanceViewModel
import kotlinx.coroutines.launch
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
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity


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
        "logout" to "Cerrar sesión",
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
        "logout" to "Log out",
        "copied_clipboard" to "Copied to Clipboard",
        "cancel" to "Cancel",
        "currency_selector" to "Preferred Currency"
    )
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BiometricLockScreen(
    labels: Map<String, String>,
    isDarkMode: Boolean,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    val scannerColor by animateColorAsState(
        targetValue = if (isChecking) Color(0xFFBAC3FF) else Color.Gray,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    fun authenticate() {
        val fragmentActivity = context as? FragmentActivity
        if (fragmentActivity == null) {
            errorMsg = "Biometric prompt requires FragmentActivity"
            return
        }

        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            fragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    isChecking = false
                    errorMsg = errString.toString()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isChecking = false
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    isChecking = false
                    errorMsg = "Authentication failed"
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(labels["bio_lock_title"] ?: "Authenticate")
            .setSubtitle(labels["bio_lock_subtitle"] ?: "Log in using your biometric credential")
            .setNegativeButtonText(labels["cancel"] ?: "Cancel")
            .build()
            
        isChecking = true
        errorMsg = null
        biometricPrompt.authenticate(promptInfo)
    }

    // Launch authentication automatically on screen display
    LaunchedEffect(Unit) {
        authenticate()
    }

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
                    authenticate()
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

        if (errorMsg != null) {
            Text(
                errorMsg!!,
                color = Color.Red,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

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
fun AddTransactionModal(
    labels: Map<String, String>,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onConfirm: (concept: String, amount: Double, category: String, bank: String, currency: String) -> Unit
) {
    var concept by remember { mutableStateOf(TextFieldValue("")) }
    var amountStr by remember { mutableStateOf(TextFieldValue("")) }
    var selectedCategory by remember { mutableStateOf("Alimentos") }
    var selectedBank by remember { mutableStateOf("Manual") }
    var selectedCurrency by remember { mutableStateOf("EUR") }
    var userModifiedCategory by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(false) }

    LaunchedEffect(concept.text) {
        if (!userModifiedCategory && concept.text.length > 2) {
            kotlinx.coroutines.delay(300)
            val predicted = viewModel.predictCategory(concept.text)
            if (predicted.isNotEmpty()) {
                selectedCategory = predicted
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            isAnalyzing = true
            coroutineScope.launch {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val byteArray = stream.toByteArray()
                val extracted = viewModel.extractReceiptImage(byteArray)
                if (extracted != null) {
                    concept = TextFieldValue(extracted.concept)
                    amountStr = TextFieldValue(extracted.amount.toString())
                    // Try to match the category
                    if (extracted.category in listOf("Alimentos", "Suscripción", "Transporte", "Restaurantes", "Ocio", "Inversiones", "Ingreso")) {
                        selectedCategory = extracted.category
                    }
                }
                isAnalyzing = false
            }
        }
    }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(labels["add_tx"] ?: "Add Transaction", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    
                    IconButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.testTag("scan_receipt_btn")
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Scan Receipt", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                if (isAnalyzing) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Analizando factura con Gemini Vision...", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

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
                        // Dynamically use viewModel's state or fallback list
                        val categoryItems = viewModel.categoryItems.collectAsState().value
                        val dynamicCategories = categoryItems.map { it.category }.distinct().ifEmpty { categories }
                        
                        dynamicCategories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { 
                                    selectedCategory = cat 
                                    userModifiedCategory = true
                                },
                                label = { Text(cat) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = com.example.ui.finance.getCategoryIcon(cat),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
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
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
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
        modifier = modifier
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (leadingIcon != null) {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    leadingIcon()
                }
            }
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                label()
            }
        }
    }
}

// =========================================================================
// --- CHARTS & ADVANCED INTERACTIVE ANALYTICS TAB (SOPHISTICATED DARK) ---
// =========================================================================