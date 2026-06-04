package com.example.ui.charts

import com.example.ui.shared.*
import com.example.ui.finance.FinanceViewModel

import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter

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


@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
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
    val expenses = remember(txState) { txState.filter { it.amount < 0 } }

    val monthlyTotals = remember(expenses, selectCurrency) {
        val grouped = expenses.groupBy { tx ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = tx.timestamp
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) // 0-11
            Pair(year, month)
        }
        val totals = grouped.mapValues { entry ->
            entry.value.sumOf { tx ->
                viewModel.convertCurrency(-tx.amount, tx.currency, selectCurrency)
            }
        }.toList().sortedWith(compareBy({ it.first.first }, { it.first.second }))

        if (totals.size > 12) totals.takeLast(12) else totals
    }

    if (monthlyTotals.isEmpty()) {
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
                    text = if (isSpanish) "No hay suficientes datos para generar el historial de meses." else "No historical balance stats to trace. Add some cash flows first.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        return
    }

    val model = remember(monthlyTotals) {
        val entries = monthlyTotals.mapIndexed { index, pair ->
            FloatEntry(x = index.toFloat(), y = pair.second.toFloat())
        }
        entryModelOf(entries)
    }

    val monthFormatter = remember(monthlyTotals) {
        val monthNamesEs = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        val monthNamesEn = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val idx = value.toInt()
            if (idx in monthlyTotals.indices) {
                val (_, month) = monthlyTotals[idx].first
                if (isSpanish) monthNamesEs[month] else monthNamesEn[month]
            } else {
                ""
            }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = if (isSpanish) "Historial de Gastos por Mes" else "Monthly Spending History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black
                )
                Text(
                    text = if (isSpanish) "Comparativa de consumo a lo largo del tiempo" else "Consumption comparison over time",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                Chart(
                    chart = columnChart(),
                    model = model,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(valueFormatter = monthFormatter),
                )
            }
        }
    }
}



