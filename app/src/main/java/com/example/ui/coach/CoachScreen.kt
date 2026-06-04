package com.example.ui.coach

import com.example.ui.shared.*
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