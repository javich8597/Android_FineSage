package com.example.ui.coach

import com.example.data.model.Transaction
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.finance.FinanceViewModel
import kotlinx.coroutines.launch

@Composable
fun CoachTab(viewModel: FinanceViewModel, labels: Map<String, String>, isDarkMode: Boolean) {
    val txState by viewModel.transactions.collectAsState()
    val goalsState by viewModel.goals.collectAsState()
    val petStyle by viewModel.petStyle.collectAsState()
    val petLevel by viewModel.petLevel.collectAsState()
    val petXpInLevel by viewModel.petXpInLevel.collectAsState()
    val petMoodState by viewModel.petMood.collectAsState()
    val isSpan = labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL"
    
    var isPetExpanded by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = isPetExpanded,
        transitionSpec = {
            slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) togetherWith
            slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400))
        },
        label = "PetExpansion"
    ) { expanded ->
        if (expanded) {
            InteractivePetScreen(
                viewModel = viewModel,
                isSpan = isSpan,
                isDarkMode = isDarkMode,
                transactions = txState,
                onClose = { isPetExpanded = false }
            )
        } else {
            CoachDashboard(
                viewModel = viewModel,
                isSpan = isSpan,
                isDarkMode = isDarkMode,
                transactions = txState,
                goals = goalsState,
                petStyle = petStyle,
                petLevel = petLevel,
                petXpInLevel = petXpInLevel,
                petMood = petMoodState,
                onExpandPet = { isPetExpanded = true }
            )
        }
    }
}

@Composable
fun CoachDashboard(
    viewModel: FinanceViewModel,
    isSpan: Boolean,
    isDarkMode: Boolean,
    transactions: List<Transaction>,
    goals: List<com.example.data.model.BudgetGoal>,
    petStyle: String,
    petLevel: Int,
    petXpInLevel: Int,
    petMood: PetMood,
    onExpandPet: () -> Unit
) {
    var interactionOneSolved by remember { mutableStateOf(false) }
    var interactionTwoSolved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- HERO: THE VIRTUAL PET! ---
        PetStatusSection(
            isSpan = isSpan,
            isDarkMode = isDarkMode,
            transactions = transactions,
            goals = goals,
            petStyle = petStyle,
            petLevel = petLevel,
            petXpInLevel = petXpInLevel,
            petMood = petMood,
            onClick = onExpandPet
        )

        // --- GEMINI AI PERSONALIZED MISSION & AUDIT ---
        GeminiAuditCard(isSpan, isDarkMode, viewModel)

        // --- INTERACTIVE BITE-SIZED INSIGHTS ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = if (isSpan) "Revisión rápida de hábitos" else "Quick Habit Check",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                if (!interactionOneSolved) {
                    item {
                        InteractiveInsightCard(
                            isSpan = isSpan,
                            isDarkMode = isDarkMode,
                            icon = Icons.Filled.DirectionsCar,
                            iconTint = MaterialTheme.colorScheme.error,
                            title = if (isSpan) "Aumento Transporte" else "Spike in Transport",
                            message = if (isSpan) "Tu gasto en Uber subió un 25% esta semana. ¿Quieres que fijemos un límite de 30€?" 
                                      else "Your Uber spending is up 25% this week. Want to set a 30€ limit?",
                            primaryAction = if (isSpan) "Sí, fijar límite" else "Yes, set limit",
                            secondaryAction = if (isSpan) "No, estoy bien" else "No, I'm okay",
                            onPrimary = { interactionOneSolved = true },
                            onSecondary = { interactionOneSolved = true }
                        )
                    }
                }
                
                if (!interactionTwoSolved) {
                    item {
                        InteractiveInsightCard(
                            isSpan = isSpan,
                            isDarkMode = isDarkMode,
                            icon = Icons.Filled.Restaurant,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            title = if (isSpan) "Buen ritmo Comida" else "Good pace in Dining",
                            message = if (isSpan) "Llevas 3 días sin gastos en restaurantes. Si sigues así, ahorrarás 45€ extra este mes." 
                                      else "You've gone 3 days without dining out. Keep it up to save an extra 45€ this month.",
                            primaryAction = if (isSpan) "Asignar a ahorros" else "Move to savings",
                            secondaryAction = if (isSpan) "Dejar en cartera" else "Keep in wallet",
                            onPrimary = { interactionTwoSolved = true },
                            onSecondary = { interactionTwoSolved = true }
                        )
                    }
                }
            }
        }

        // --- AI CONVERSATION (NOT A TRADITIONAL CHAT) ---
        DeepAnalysisTrigger(isSpan, isDarkMode, viewModel)
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PetStatusSection(
    isSpan: Boolean,
    isDarkMode: Boolean,
    transactions: List<Transaction>,
    goals: List<com.example.data.model.BudgetGoal>,
    petStyle: String,
    petLevel: Int,
    petXpInLevel: Int,
    petMood: PetMood,
    onClick: () -> Unit
) {
    val (petEmoji, petMoodLabel, petColor) = when (petStyle) {
        "FUTURISTIC" -> when (petMood) {
            PetMood.HAPPY -> Triple("🌟🐉", if (isSpan) "Ciber-Dragón Legendario" else "Legendary Cyber-Dragon", Color(0xFF10B981))
            PetMood.IDLE -> Triple("🤖🐉", if (isSpan) "Ciber-Dragón Saludable" else "Healthy Cyber-Dragon", Color(0xFFF59E0B))
            else -> Triple("🐲🔥", if (isSpan) "Ciber-Dragón Hambriento" else "Starving Cyber-Dragon", Color(0xFFEF4444))
        }
        "ZEN" -> when (petMood) {
            PetMood.HAPPY -> Triple("🌸🌳", if (isSpan) "Árbol Zen Floreciente" else "Blooming Zen Tree", Color(0xFF10B981))
            PetMood.IDLE -> Triple("🌳", if (isSpan) "Árbol Zen Estable" else "Stable Zen Tree", Color(0xFF34D399))
            else -> Triple("🥀", if (isSpan) "Árbol Zen Marchito" else "Wilting Zen Tree", Color(0xFFEF4444))
        }
        else -> when (petMood) {
            PetMood.HAPPY -> Triple("🐷✨", if (isSpan) "Mascota Feliz" else "Happy Pet", Color(0xFFF472B6))
            PetMood.IDLE -> Triple("🐷", if (isSpan) "Mascota Estable" else "Stable Pet", Color(0xFFFBBF24))
            else -> Triple("🐷💢", if (isSpan) "Mascota Molesta" else "Angry Pet", Color(0xFFEF4444))
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 4.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
                // Holographic Pet Orb (3D look)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    petColor.copy(alpha = 0.8f),
                                    petColor.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(2.dp, petColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Specular highlight for 3D glassy effect
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.4f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.4f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    AnimatedContent(
                        targetState = petEmoji,
                        transitionSpec = {
                            scaleIn() togetherWith fadeOut()
                        },
                        label = "PetFaceMiniAnimation"
                    ) { targetEmoji ->
                        Text(targetEmoji, fontSize = 28.sp)
                    }
                }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = if (isSpan) "Tu Mascota Financiera" else "Your Financial Pet",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = petMoodLabel,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { 
                        val xpProgress = petXpInLevel.toFloat() / 1000f
                        if (xpProgress.isNaN()) 0f else xpProgress.coerceIn(0f, 1f) 
                    },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = petColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isSpan) "LVL $petLevel • EXP: $petXpInLevel / 1000" else "LVL $petLevel • XP: $petXpInLevel / 1000",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GeminiAuditCard(isSpan: Boolean, isDarkMode: Boolean, viewModel: FinanceViewModel) {
    val coachingResponse by viewModel.coachingResponse.collectAsState()
    val isCoachingLoading by viewModel.isCoachingLoading.collectAsState()
    var selectedTimeframe by remember { mutableStateOf("weekly") }

    val bgColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFEFF6FF)
    val borderColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFBFDBFE)

    var diagnosis by remember { mutableStateOf("") }
    var mission by remember { mutableStateOf("") }
    var rawText by remember { mutableStateOf("") }

    // Parse JSON
    LaunchedEffect(coachingResponse) {
        if (coachingResponse.isNotEmpty()) {
            try {
                // strip markdown if it exists
                val jsonStr = coachingResponse.replace("```json", "").replace("```", "").trim()
                val json = org.json.JSONObject(jsonStr)
                diagnosis = json.optString("diagnosis")
                mission = json.optString("mission")
                rawText = ""
            } catch (e: Exception) {
                diagnosis = ""
                mission = ""
                rawText = coachingResponse
            }
        } else {
            diagnosis = ""
            mission = ""
            rawText = ""
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Reporte Inteligente",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Timeframe Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("daily" to "Diario", "weekly" to "Semanal", "monthly" to "Mensual").forEach { (key, label) ->
                    FilterChip(
                        selected = selectedTimeframe == key,
                        onClick = { selectedTimeframe = key; viewModel.askCoachingAdvisor(key) },
                        label = { Text(label, fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isCoachingLoading) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Analizando tus finanzas...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (diagnosis.isNotEmpty() && mission.isNotEmpty()) {
                // --- VISUAL REPORT ---
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.QueryStats, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DIAGNÓSTICO", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(diagnosis, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MISIÓN", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(mission, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
                    }
                }
            } else if (rawText.isNotEmpty()) {
                Text(
                    text = rawText,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = "Selecciona un rango de tiempo para generar tu reporte.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun InteractiveInsightCard(
    isSpan: Boolean, 
    isDarkMode: Boolean, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    iconTint: Color, 
    title: String, 
    message: String,
    primaryAction: String,
    secondaryAction: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.width(280.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                minLines = 3 // Keep uniform height
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPrimary,
                    colors = ButtonDefaults.buttonColors(containerColor = iconTint),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(primaryAction, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onSecondary,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(secondaryAction, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun DeepAnalysisTrigger(isSpan: Boolean, isDarkMode: Boolean, viewModel: FinanceViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var isThinking by remember { mutableStateOf(false) }
    var aiResponse by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2E1065) else Color(0xFFF3E8FF) // Purple tone
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isSpan) "Consulta Estratégica" else "Strategic Chat",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSpan) "¿Quieres profundizar en algo? Pregúntame sobre tus patrones financieros de este mes y te daré un resumen claro, no una charla interminable." 
                       else "Want to dive deeper? Ask me about your financial patterns this month and I'll give you a clear summary, not an endless chat.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = if (isDarkMode) Color(0xFFE9D5FF) else Color(0xFF6B21A8)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (aiResponse.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkMode) Color(0xFF1E1B4B) else Color.White)
                        .padding(16.dp)
                ) {
                    Text(
                        text = aiResponse, 
                        fontSize = 14.sp, 
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Mock Input Field
            var query by remember { mutableStateOf("") }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { 
                    Text(
                        if (isSpan) "¿Cómo puedo optimizar mi pago de impuestos?" else "How can I optimize my tax payments?",
                        fontSize = 13.sp
                    ) 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha=0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                trailingIcon = {
                    if (isThinking) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.secondary, strokeWidth = 2.dp)
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                                .clickable {
                                    if(query.isNotEmpty()) {
                                        isThinking = true
                                        // Simulate AI network call to a real AI / Gemini logic
                                        coroutineScope.launch {
                                            viewModel.askCoachingAdvisor() // using the existing one
                                            kotlinx.coroutines.delay(2000)
                                            aiResponse = if (isSpan) "He analizado tus datos: \n1. Tienes 4 suscripciones que apenas usas.\n2. Tu fondo de emergencia podría crecer más rápido en una cuenta remunerada (Trade Republic da un 2%).\n\nCambiar estas dos cosas mejorará tu salud financiera un 15% este año."
                                                         else "I've analyzed your data:\n1. You have 4 barely used subscriptions.\n2. Your emergency fund could grow faster in a high-yield account (Trade Republic offers 2%).\n\nAdjusting these two will improve your financial health by 15% this year."
                                            isThinking = false
                                            query = ""
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            )
        }
    }
}
