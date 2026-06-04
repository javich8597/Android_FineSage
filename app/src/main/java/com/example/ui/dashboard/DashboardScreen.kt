package com.example.ui.dashboard

import com.example.ui.shared.*
import com.example.ui.history.*
import com.example.ui.finance.FinanceViewModel


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

        // SIMULATOR ZONA
        item {
            SavingsGoalSimulator(labels = labels, isDarkMode = isDarkMode)
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