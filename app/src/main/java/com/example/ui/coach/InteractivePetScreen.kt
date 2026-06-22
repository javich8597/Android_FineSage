package com.example.ui.coach

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.data.model.Transaction
import com.example.ui.finance.FinanceViewModel
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.graphics.ImageBitmap
import com.example.data.network.GeminiApiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

enum class PetType { PIG, CAT, DOG, DRAGON_FUTURISTIC, ZEN_TREE }
enum class PetMood { IDLE, PETTING, HAPPY, ANGRY }

@Composable
fun InteractivePetScreen(
    viewModel: FinanceViewModel,
    isSpan: Boolean,
    isDarkMode: Boolean,
    transactions: List<Transaction>,
    onClose: () -> Unit
) {
    val goals by viewModel.goals.collectAsState()
    val petStyle by viewModel.petStyle.collectAsState()
    
    var currentPetType by remember { mutableStateOf(PetType.PIG) }
    var currentMood by remember { mutableStateOf(PetMood.IDLE) }
    var isSelectingType by remember { mutableStateOf(false) }

    LaunchedEffect(petStyle) {
        when(petStyle) {
            "FUTURISTIC" -> { currentPetType = PetType.DRAGON_FUTURISTIC; isSelectingType = false }
            "ZEN" -> { currentPetType = PetType.ZEN_TREE; isSelectingType = false }
            "TRADITIONAL" -> {
                if (currentPetType !in listOf(PetType.PIG, PetType.CAT, PetType.DOG)) {
                    currentPetType = PetType.PIG
                }
            }
        }
    }

    val haptic = LocalHapticFeedback.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val scope = rememberCoroutineScope()

    val totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.amount < 0 }.sumOf { it.amount }
    val rawSavingsRate = if (totalIncome > 0) (totalIncome + totalExpense) / totalIncome else 0.0
    val savingsRate = if (rawSavingsRate.isNaN()) 0.0 else rawSavingsRate

    val totalGoalProgress = if (goals.isEmpty()) 0.0 else goals.map { if (it.targetAmount > 0) it.savedAmount / it.targetAmount else 0.0 }.average()
    val combinedScore = (savingsRate + totalGoalProgress) / 2.0

    // Decide base mood based on finance, unless currently being petted
    val financialBaseMood = when {
        combinedScore > 0.4 || savingsRate > 0.3 -> PetMood.HAPPY
        combinedScore > 0.1 || savingsRate > 0.0 -> PetMood.IDLE
        else -> PetMood.ANGRY
    }

    // Actual visual mood
    val displayMood = if (currentMood == PetMood.PETTING || currentMood == PetMood.HAPPY && financialBaseMood != PetMood.HAPPY) currentMood else financialBaseMood

    val (petEmoji, petTitle, petColor, bounceSpeed) = when (currentPetType) {
        PetType.PIG -> listOf(
            when(displayMood) { PetMood.HAPPY -> "🐷✨"; PetMood.PETTING -> "🐽💕"; PetMood.ANGRY -> "🐷💢"; else -> "🐷" },
            if(isSpan) "Cerdito Ahorrador" else "Penny Pig", Color(0xFFF472B6), 1200
        )
        PetType.CAT -> listOf(
            when(displayMood) { PetMood.HAPPY -> "😻"; PetMood.PETTING -> "😽💞"; PetMood.ANGRY -> "😾"; else -> "🐱" },
            if(isSpan) "Gato Financiero" else "Finance Cat", Color(0xFFFBBF24), 1000
        )
        PetType.DOG -> listOf(
            when(displayMood) { PetMood.HAPPY -> "🐶🦴"; PetMood.PETTING -> "🐕❤️"; PetMood.ANGRY -> "🐺"; else -> "🐶" },
            if(isSpan) "Perro Guardián" else "Guardian Dog", Color(0xFF8B5CF6), 1100
        )
        PetType.DRAGON_FUTURISTIC -> listOf(
            when(displayMood) { PetMood.HAPPY -> "🌟🐉"; PetMood.PETTING -> "🐉⚡"; PetMood.ANGRY -> "🐲🔥"; else -> "🤖🐉" },
            if(isSpan) "Ciber-Dragón" else "Cyber-Dragon", Color(0xFF10B981), 1500
        )
        PetType.ZEN_TREE -> listOf(
            when(displayMood) { PetMood.HAPPY -> "🌸🌳"; PetMood.PETTING -> "🍃✨"; PetMood.ANGRY -> "🥀"; else -> "🌳" },
            if(isSpan) "Árbol Zen" else "Zen Tree", Color(0xFF34D399), 3000
        )
    }
    
    val petDesc = when (financialBaseMood) {
        PetMood.HAPPY -> if(isSpan) "¡Tus finanzas son de leyenda!" else "Your finances are legendary!"
        PetMood.IDLE -> if(isSpan) "Vas por muy buen camino." else "You are on a good path."
        else -> if(isSpan) "Hambriento: Gastando más de la cuenta." else "Starving: Spending a bit too much."
    }

    val colorPrimary = petColor as Color
    val animationSpeedMs = bounceSpeed as Int

    // Breathing & Floating Animation
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (currentMood == PetMood.PETTING) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (currentMood == PetMood.PETTING) 300 else animationSpeedMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Wiggle effect when petting
    val rotation by infiniteTransition.animateFloat(
        initialValue = if (currentMood == PetMood.PETTING) -5f else 0f,
        targetValue = if (currentMood == PetMood.PETTING) 5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationSpeedMs + 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Realistic bouncing drag physics (Pou-style)
    val petOffsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val petOffsetY = remember { androidx.compose.animation.core.Animatable(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (isSpan) "Tu Mascota" else "Your Pet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            if (petStyle == "TRADITIONAL") {
                IconButton(
                    onClick = { isSelectingType = !isSelectingType },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(Icons.Filled.Brush, contentDescription = "Change Pet Type", tint = colorPrimary)
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp)) // Placeholder to balance ArrowBack
            }
        }

        AnimatedVisibility(visible = isSelectingType && petStyle == "TRADITIONAL") {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val types = listOf(PetType.PIG, PetType.CAT, PetType.DOG)
                items(types) { type ->
                    FilterChip(
                        selected = type == currentPetType,
                        onClick = { currentPetType = type; isSelectingType = false },
                        label = { Text(type.name.replace("_", " ")) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colorPrimary.copy(0.2f))
                    )
                }
            }
        }

        Text(
            text = if (isSpan) "✨ ¡Acaríciame! ✨" else "✨ Pet me! ✨",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // --- 1. 2D CANVAS PET IN THE MIDDLE ---
        AnimatedLivingPet(
            petType = currentPetType, 
            petMood = currentMood, 
            mainColor = colorPrimary,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            onInteractStart = { currentMood = PetMood.PETTING },
            onInteractEnd = { 
                scope.launch {
                    currentMood = PetMood.HAPPY
                    kotlinx.coroutines.delay(2000)
                    if (currentMood == PetMood.HAPPY) {
                        currentMood = financialBaseMood
                    }
                }
            }
        )

        // --- 2. BOTTOM DETAILS INFO ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
            ),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = colorPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = petTitle as String,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = colorPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = petDesc,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Experience Bar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("LVL 3", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if(isSpan) "1200 / 2000 EXP" else "1200 / 2000 XP",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val safeProgress = if (savingsRate < 0) 0.1f else savingsRate.toFloat()
                val finalProgress = if (safeProgress.isNaN()) 0f else safeProgress.coerceIn(0f, 1f)
                
                LinearProgressIndicator(
                    progress = { finalProgress },
                    modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)),
                    color = colorPrimary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha=0.1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chat Button
        Button(
            onClick = { onClose() },
            colors = ButtonDefaults.buttonColors(containerColor = colorPrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(56.dp)
        ) {
            Text(if (isSpan) "Misiones & Auditoría" else "Missions & Audit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AnimatedLivingPet(
    petType: PetType, 
    petMood: PetMood, 
    mainColor: Color, 
    modifier: Modifier = Modifier.fillMaxSize(),
    onInteractStart: () -> Unit,
    onInteractEnd: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pet")
    
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (petMood == PetMood.PETTING) 400 else 1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    val wagRotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (petMood == PetMood.PETTING || petMood == PetMood.HAPPY) 150 else 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wag"
    )

    val earRotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (petMood == PetMood.PETTING) 250 else 1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ears"
    )
    
    val floatY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val dynamicBgColor by animateColorAsState(
        targetValue = when (petMood) {
            PetMood.ANGRY -> Color.Red.copy(alpha = 0.05f)
            PetMood.HAPPY -> mainColor.copy(alpha = 0.15f)
            PetMood.PETTING -> mainColor.copy(alpha = 0.2f)
            else -> Color.Transparent
        }, 
        animationSpec = tween(1000),
        label = "bg"
    )

    Box(
        modifier = modifier
            .background(dynamicBgColor)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onInteractStart() },
                    onDragEnd = { onInteractEnd() },
                    onDragCancel = { onInteractEnd() },
                    onDrag = { _, _ -> }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(250.dp)) {
            val cx = size.width / 2
            val cy = size.height / 2 + floatY
            val baseRadius = size.minDimension / 3.5f * breathingScale

            translate(top = floatY) {
                when (petType) {
                    PetType.CAT -> drawCatPuppet(cx, size.height / 2, baseRadius, mainColor, petMood, wagRotation, earRotation)
                    PetType.PIG -> drawPigPuppet(cx, size.height / 2, baseRadius, mainColor, petMood, wagRotation, earRotation)
                    PetType.DOG -> drawDogPuppet(cx, size.height / 2, baseRadius, mainColor, petMood, wagRotation, earRotation)
                    PetType.DRAGON_FUTURISTIC -> drawDragonPuppet(cx, size.height / 2, baseRadius, mainColor, petMood, wagRotation)
                    PetType.ZEN_TREE -> drawTreePuppet(cx, size.height / 2, baseRadius, mainColor, petMood, wagRotation)
                }
            }
        }
    }
}

fun DrawScope.drawCatPuppet(cx: Float, cy: Float, radius: Float, color: Color, mood: PetMood, wag: Float, earRot: Float) {
    // Tail
    rotate(wag, pivot = androidx.compose.ui.geometry.Offset(cx + radius * 0.8f, cy + radius * 0.5f)) {
        val tailPath = Path().apply {
            moveTo(cx + radius * 0.8f, cy + radius * 0.5f)
            quadraticBezierTo(cx + radius * 1.5f, cy, cx + radius * 1.8f, cy - radius * 0.5f)
        }
        drawPath(tailPath, color, style = Stroke(width = radius * 0.3f, cap = StrokeCap.Round))
    }

    // Body
    drawCircle(color, radius = radius, center = androidx.compose.ui.geometry.Offset(cx, cy))
    
    // Ears
    val earColor = color.copy(alpha = 0.8f)
    // Left Ear
    rotate(-earRot, pivot = androidx.compose.ui.geometry.Offset(cx - radius * 0.6f, cy - radius * 0.6f)) {
        val path = Path().apply {
            moveTo(cx - radius * 0.9f, cy - radius * 0.3f)
            lineTo(cx - radius * 0.8f, cy - radius * 1.2f)
            lineTo(cx - radius * 0.2f, cy - radius * 0.8f)
            close()
        }
        drawPath(path, earColor)
    }
    // Right Ear
    rotate(earRot, pivot = androidx.compose.ui.geometry.Offset(cx + radius * 0.6f, cy - radius * 0.6f)) {
        val path = Path().apply {
            moveTo(cx + radius * 0.9f, cy - radius * 0.3f)
            lineTo(cx + radius * 0.8f, cy - radius * 1.2f)
            lineTo(cx + radius * 0.2f, cy - radius * 0.8f)
            close()
        }
        drawPath(path, earColor)
    }

    // Face
    drawFace(cx, cy, radius, mood)
}

fun DrawScope.drawPigPuppet(cx: Float, cy: Float, radius: Float, color: Color, mood: PetMood, wag: Float, earRot: Float) {
    // Tail
    rotate(wag, pivot = androidx.compose.ui.geometry.Offset(cx + radius * 0.9f, cy + radius * 0.2f)) {
        val tailPath = Path().apply {
            moveTo(cx + radius * 0.8f, cy + radius * 0.2f)
            cubicTo(
                cx + radius * 1.2f, cy - radius * 0.2f,
                cx + radius * 1.4f, cy + radius * 0.4f,
                cx + radius * 1.1f, cy + radius * 0.3f
            )
        }
        drawPath(tailPath, color.copy(alpha = 0.8f), style = Stroke(width = radius * 0.15f, cap = StrokeCap.Round))
    }

    // Body
    drawCircle(color, radius = radius, center = androidx.compose.ui.geometry.Offset(cx, cy))
    
    // Snout
    drawOval(
        color.copy(alpha = 0.6f),
        topLeft = androidx.compose.ui.geometry.Offset(cx - radius * 0.4f, cy + radius * 0.1f),
        size = androidx.compose.ui.geometry.Size(radius * 0.8f, radius * 0.5f)
    )
    // Snout holes
    drawCircle(Color.DarkGray, radius = radius * 0.08f, center = androidx.compose.ui.geometry.Offset(cx - radius * 0.15f, cy + radius * 0.35f))
    drawCircle(Color.DarkGray, radius = radius * 0.08f, center = androidx.compose.ui.geometry.Offset(cx + radius * 0.15f, cy + radius * 0.35f))

    // Ears
    val earColor = color.copy(alpha = 0.9f)
    rotate(-earRot, pivot = androidx.compose.ui.geometry.Offset(cx - radius * 0.6f, cy - radius * 0.7f)) {
        drawOval(earColor, topLeft = androidx.compose.ui.geometry.Offset(cx - radius * 1.1f, cy - radius * 1.1f), size = androidx.compose.ui.geometry.Size(radius * 0.6f, radius * 0.6f))
    }
    rotate(earRot, pivot = androidx.compose.ui.geometry.Offset(cx + radius * 0.6f, cy - radius * 0.7f)) {
        drawOval(earColor, topLeft = androidx.compose.ui.geometry.Offset(cx + radius * 0.5f, cy - radius * 1.1f), size = androidx.compose.ui.geometry.Size(radius * 0.6f, radius * 0.6f))
    }

    // Face (eyes above snout)
    drawFace(cx, cy - radius * 0.2f, radius, mood)
}

fun DrawScope.drawDogPuppet(cx: Float, cy: Float, radius: Float, color: Color, mood: PetMood, wag: Float, earRot: Float) {
    // Tail
    rotate(wag * 2, pivot = androidx.compose.ui.geometry.Offset(cx + radius * 0.8f, cy + radius * 0.4f)) {
        val tailPath = Path().apply {
            moveTo(cx + radius * 0.7f, cy + radius * 0.4f)
            lineTo(cx + radius * 1.5f, cy + radius * 0.1f)
        }
        drawPath(tailPath, color, style = Stroke(width = radius * 0.3f, cap = StrokeCap.Round))
    }

    // Body
    drawCircle(color, radius = radius, center = androidx.compose.ui.geometry.Offset(cx, cy))

    // Ears (Floppy)
    rotate(earRot, pivot = androidx.compose.ui.geometry.Offset(cx - radius * 0.7f, cy - radius * 0.3f)) {
        drawOval(color.copy(alpha = 0.8f), topLeft = androidx.compose.ui.geometry.Offset(cx - radius * 1.2f, cy - radius * 0.3f), size = androidx.compose.ui.geometry.Size(radius * 0.5f, radius * 1.0f))
    }
    rotate(-earRot, pivot = androidx.compose.ui.geometry.Offset(cx + radius * 0.7f, cy - radius * 0.3f)) {
        drawOval(color.copy(alpha = 0.8f), topLeft = androidx.compose.ui.geometry.Offset(cx + radius * 0.7f, cy - radius * 0.3f), size = androidx.compose.ui.geometry.Size(radius * 0.5f, radius * 1.0f))
    }

    // Muzzle
    drawCircle(Color.White, radius = radius * 0.4f, center = androidx.compose.ui.geometry.Offset(cx, cy + radius * 0.3f))
    // Nose
    drawCircle(Color.Black, radius = radius * 0.12f, center = androidx.compose.ui.geometry.Offset(cx, cy + radius * 0.2f))

    // Face
    drawFace(cx, cy - radius * 0.1f, radius, mood)
}

fun DrawScope.drawDragonPuppet(cx: Float, cy: Float, radius: Float, color: Color, mood: PetMood, wag: Float) {
    // Angular body
    val bodyPath = Path().apply {
        moveTo(cx, cy - radius)
        lineTo(cx + radius, cy)
        lineTo(cx, cy + radius)
        lineTo(cx - radius, cy)
        close()
    }
    drawPath(bodyPath, color)

    // Glowing core
    drawCircle(Color.Cyan, radius = radius * 0.4f, center = androidx.compose.ui.geometry.Offset(cx, cy))

    // Floating outer bits rotating (simulated by wag)
    rotate(wag * 4, pivot = androidx.compose.ui.geometry.Offset(cx, cy)) {
        val outerRing = Path().apply {
            addRect(androidx.compose.ui.geometry.Rect(cx - radius * 1.2f, cy - radius * 0.1f, cx + radius * 1.2f, cy + radius * 0.1f))
            addRect(androidx.compose.ui.geometry.Rect(cx - radius * 0.1f, cy - radius * 1.2f, cx + radius * 0.1f, cy + radius * 1.2f))
        }
        drawPath(outerRing, color.copy(alpha = 0.5f))
    }

    // Eyes
    val eyeColor = if (mood == PetMood.ANGRY) Color.Red else Color.Cyan
    drawCircle(eyeColor, radius = radius * 0.1f, center = androidx.compose.ui.geometry.Offset(cx - radius * 0.3f, cy - radius * 0.4f))
    drawCircle(eyeColor, radius = radius * 0.1f, center = androidx.compose.ui.geometry.Offset(cx + radius * 0.3f, cy - radius * 0.4f))
}

fun DrawScope.drawTreePuppet(cx: Float, cy: Float, radius: Float, color: Color, mood: PetMood, wag: Float) {
    // Trunk
    drawRect(
        Color(0xFF8B5A2B), 
        topLeft = androidx.compose.ui.geometry.Offset(cx - radius * 0.2f, cy),
        size = androidx.compose.ui.geometry.Size(radius * 0.4f, radius * 1.5f)
    )

    // Canopy swaying
    rotate(wag * 0.5f, pivot = androidx.compose.ui.geometry.Offset(cx, cy + radius)) {
        val canopyColor = when(mood) {
            PetMood.ANGRY -> Color(0xFF6B8E23)
            PetMood.HAPPY, PetMood.PETTING -> color
            else -> color.copy(alpha = 0.8f)
        }
        drawCircle(canopyColor, radius = radius * 1.2f, center = androidx.compose.ui.geometry.Offset(cx, cy - radius * 0.2f))
        drawCircle(canopyColor, radius = radius * 0.8f, center = androidx.compose.ui.geometry.Offset(cx - radius * 0.8f, cy + radius * 0.2f))
        drawCircle(canopyColor, radius = radius * 0.8f, center = androidx.compose.ui.geometry.Offset(cx + radius * 0.8f, cy + radius * 0.2f))
    }

    // Face on trunk
    drawFace(cx, cy + radius * 0.5f, radius * 0.5f, mood)
}

fun DrawScope.drawFace(cx: Float, cy: Float, radius: Float, mood: PetMood) {
    val eyeColor = Color.DarkGray
    val eyeRadius = radius * 0.12f
    val eyeY = cy - radius * 0.2f

    when (mood) {
        PetMood.HAPPY, PetMood.PETTING -> {
            // Happy closed eyes ^ ^
            val leftEye = Path().apply {
                moveTo(cx - radius * 0.4f, eyeY)
                quadraticBezierTo(cx - radius * 0.3f, eyeY - radius * 0.2f, cx - radius * 0.2f, eyeY)
            }
            val rightEye = Path().apply {
                moveTo(cx + radius * 0.2f, eyeY)
                quadraticBezierTo(cx + radius * 0.3f, eyeY - radius * 0.2f, cx + radius * 0.4f, eyeY)
            }
            drawPath(leftEye, eyeColor, style = Stroke(width = radius * 0.08f, cap = StrokeCap.Round))
            drawPath(rightEye, eyeColor, style = Stroke(width = radius * 0.08f, cap = StrokeCap.Round))
            
            // Smile
            val smile = Path().apply {
                moveTo(cx - radius * 0.15f, cy + radius * 0.1f)
                quadraticBezierTo(cx, cy + radius * 0.3f, cx + radius * 0.15f, cy + radius * 0.1f)
            }
            drawPath(smile, eyeColor, style = Stroke(width = radius * 0.08f, cap = StrokeCap.Round))
        }
        PetMood.ANGRY -> {
            // Angry angled eyes \ /
            val leftEye = Path().apply { moveTo(cx - radius * 0.4f, eyeY - radius * 0.1f); lineTo(cx - radius * 0.2f, eyeY + radius * 0.1f) }
            val rightEye = Path().apply { moveTo(cx + radius * 0.4f, eyeY - radius * 0.1f); lineTo(cx + radius * 0.2f, eyeY + radius * 0.1f) }
            drawPath(leftEye, eyeColor, style = Stroke(width = radius * 0.08f, cap = StrokeCap.Round))
            drawPath(rightEye, eyeColor, style = Stroke(width = radius * 0.08f, cap = StrokeCap.Round))
            
            // Frown
            val frown = Path().apply {
                moveTo(cx - radius * 0.15f, cy + radius * 0.2f)
                quadraticBezierTo(cx, cy + radius * 0.05f, cx + radius * 0.15f, cy + radius * 0.2f)
            }
            drawPath(frown, eyeColor, style = Stroke(width = radius * 0.08f, cap = StrokeCap.Round))
        }
        else -> {
            // Idle Normal eyes
            drawCircle(eyeColor, radius = eyeRadius, center = androidx.compose.ui.geometry.Offset(cx - radius * 0.3f, eyeY))
            // Blinking logic can be added later, but standard open for now.
            drawCircle(eyeColor, radius = eyeRadius, center = androidx.compose.ui.geometry.Offset(cx + radius * 0.3f, eyeY))
            
            // Small mouth
            drawCircle(eyeColor, radius = radius * 0.04f, center = androidx.compose.ui.geometry.Offset(cx, cy + radius * 0.15f))
        }
    }
}


