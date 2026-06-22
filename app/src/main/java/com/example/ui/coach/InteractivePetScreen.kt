package com.example.ui.coach

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.testTag
import com.example.data.model.Transaction
import com.example.ui.finance.FinanceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// SceneView imports
import io.github.sceneview.Scene
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation

enum class PetType { PIG, CAT, DOG, DRAGON_FUTURISTIC, ZEN_TREE }
enum class PetMood { IDLE, PETTING, HAPPY, ANGRY }
enum class PetActionState { IDLE_SITTING, IDLE_WALKING, PLAYING_HIDE, SAD_BACK }

@Composable
fun InteractivePetScreen(
    viewModel: FinanceViewModel,
    isSpan: Boolean,
    isDarkMode: Boolean,
    transactions: List<Transaction>,
    onClose: () -> Unit
) {
    val showMissionsDialog = remember { mutableStateOf(false) }
    if (showMissionsDialog.value) {
        MissionsDialog(viewModel, isSpan) { showMissionsDialog.value = false }
    }
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

    val petLevel by viewModel.petLevel.collectAsState()
    val petXpInLevel by viewModel.petXpInLevel.collectAsState()
    val financialBaseMood by viewModel.petMood.collectAsState()

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val displayMood = if (currentMood == PetMood.PETTING || currentMood == PetMood.HAPPY && financialBaseMood != PetMood.HAPPY) currentMood else financialBaseMood

    val petTitle = when (currentPetType) {
        PetType.PIG -> when {
            petLevel >= 5 -> if(isSpan) "S�per Cerdito Dorado" else "Golden Hog"
            petLevel >= 3 -> if(isSpan) "Cerdito Ahorrador" else "Penny Pig"
            else -> if(isSpan) "Cerdito Beb�" else "Penny Piglet"
        }
        PetType.CAT -> when {
            petLevel >= 5 -> if(isSpan) "Pantera Acumuladora" else "Wealthy Panther"
            petLevel >= 3 -> if(isSpan) "Gato Financiero" else "Finance Cat"
            else -> if(isSpan) "Gatito Inversor" else "Finance Kitten"
        }
        PetType.DOG -> when {
            petLevel >= 5 -> if(isSpan) "Cerbero Protector" else "Guardian Cerberus"
            petLevel >= 3 -> if(isSpan) "Perro Guardi�n" else "Guardian Dog"
            else -> if(isSpan) "Cachorro Guardi�n" else "Guardian Pup"
        }
        PetType.DRAGON_FUTURISTIC -> when {
            petLevel >= 5 -> if(isSpan) "Cyber-Wyvern Supremo" else "Cyber-Wyvern"
            petLevel >= 3 -> if(isSpan) "Ciber-Drag�n" else "Cyber-Dragon"
            else -> if(isSpan) "Cyber-Hatchling" else "Cyber-Hatchling"
        }
        PetType.ZEN_TREE -> when {
            petLevel >= 5 -> if(isSpan) "Anciano �rbol Zen" else "Zen Forest Elder"
            petLevel >= 3 -> if(isSpan) "�rbol Zen" else "Zen Tree"
            else -> if(isSpan) "Brote Zen" else "Zen Seedling"
        }
    }

    val (_, _, petColor, bounceSpeed) = when (currentPetType) {
        PetType.PIG -> listOf("", "", Color(0xFFF472B6), 1200)
        PetType.CAT -> listOf("", "", Color(0xFFFBBF24), 1000)
        PetType.DOG -> listOf("", "", Color(0xFF8B5CF6), 1100)
        PetType.DRAGON_FUTURISTIC -> listOf("", "", Color(0xFF10B981), 1500)
        PetType.ZEN_TREE -> listOf("", "", Color(0xFF34D399), 3000)
    }
    
    val petDesc = when (financialBaseMood) {
        PetMood.HAPPY -> if(isSpan) "�Tus finanzas son de leyenda!" else "Your finances are legendary!"
        PetMood.IDLE -> if(isSpan) "Vas por muy buen camino." else "You are on a good path."
        else -> if(isSpan) "Hambriento: Gastando m�s de la cuenta." else "Starving: Spending a bit too much."
    }

    val colorPrimary = petColor as Color

    if (petStyle.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isSpan) "Elige tu Mascota" else "Choose your Pet",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSpan) "Selecciona un estilo para comenzar tu viaje financiero" else "Select a style to begin your financial journey",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            listOf(
                Triple("TRADITIONAL", if (isSpan) "Tradicional" else "Traditional", "??????"),
                Triple("ZEN", "Zen", "??????"),
                Triple("FUTURISTIC", if (isSpan) "Futurista" else "Futuristic", "?????")
            ).forEach { (style, label, emojis) ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { viewModel.setPetStyle(style) }
                        .testTag("pet_style_$style")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = emojis,
                            fontSize = 32.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Column {
                            Text(
                                text = label,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when(style) {
                                    "TRADITIONAL" -> if (isSpan) "Mascotas animadas cl�sicas" else "Classic animated pets"
                                    "ZEN" -> if (isSpan) "Un bons�i interactivo relajante" else "A relaxing interactive bonsai"
                                    else -> if (isSpan) "Una criatura digital cibern�tica" else "A cybernetic digital creature"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            TextButton(onClick = onClose) {
                Text(if (isSpan) "Atr�s" else "Back")
            }
        }
    } else {
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    Spacer(modifier = Modifier.size(48.dp)) // Placeholder
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
                text = if (isSpan) "? �Acar�ciame! ?" else "? Pet me! ?",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // 3D PET
            AnimatedLivingPet(
                petStyle = petStyle, 
                petMood = currentMood, 
                mainColor = colorPrimary,
                petLevel = petLevel,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                onInteractStart = { currentMood = PetMood.PETTING },
                onInteractEnd = { 
                    scope.launch {
                        currentMood = PetMood.HAPPY
                        delay(2000)
                        if (currentMood == PetMood.HAPPY) {
                            currentMood = financialBaseMood
                        }
                    }
                }
            )

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
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("LVL $petLevel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if(isSpan) "$petXpInLevel / 1000 EXP" else "$petXpInLevel / 1000 XP",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val xpProgress = petXpInLevel.toFloat() / 1000f
                    val finalProgress = if (xpProgress.isNaN()) 0f else xpProgress.coerceIn(0f, 1f)
                    
                    LinearProgressIndicator(
                        progress = { finalProgress },
                        modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)),
                        color = colorPrimary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha=0.1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showMissionsDialog.value = true; viewModel.requestAuditAndMissions() },
                colors = ButtonDefaults.buttonColors(containerColor = colorPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(56.dp)
            ) {
                Text(if (isSpan) "Misiones & Auditoría" else "Missions & Audit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onClose() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(56.dp)
            ) {
                Text(if (isSpan) "Cerrar" else "Close", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AnimatedLivingPet(
    petStyle: String, 
    petMood: PetMood, 
    mainColor: Color, 
    petLevel: Int = 1,
    modifier: Modifier = Modifier.fillMaxSize(),
    onInteractStart: () -> Unit,
    onInteractEnd: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var actionState by remember { mutableStateOf(PetActionState.IDLE_SITTING) }
    
    var dragOffsetX by remember { mutableStateOf(0f) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    
    val animatedDragOffsetX by animateFloatAsState(targetValue = dragOffsetX, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    val animatedDragOffsetY by animateFloatAsState(targetValue = dragOffsetY, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))

    LaunchedEffect(petMood) {
        if (petMood == PetMood.ANGRY) {
            actionState = PetActionState.SAD_BACK
        } else if (petMood == PetMood.PETTING) {
            actionState = PetActionState.PLAYING_HIDE
            delay(1500)
        } else {
            while (true) {
                val willWalk = (0..1).random() == 1
                if (willWalk) {
                    actionState = PetActionState.IDLE_WALKING
                    delay(3500)
                } else {
                    actionState = PetActionState.IDLE_SITTING
                    delay((2000..5000).random().toLong())
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val isSad = actionState == PetActionState.SAD_BACK
    
    val interactScale by animateFloatAsState(targetValue = if (petMood == PetMood.PETTING) 1.1f else 1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))

    val dynamicBgColor by androidx.compose.animation.animateColorAsState(
        targetValue = when (petMood) {
            PetMood.ANGRY -> Color.DarkGray.copy(alpha = 0.15f)
            PetMood.HAPPY -> mainColor.copy(alpha = 0.15f)
            PetMood.PETTING -> mainColor.copy(alpha = 0.25f)
            else -> Color.Transparent
        }, 
        animationSpec = androidx.compose.animation.core.tween(1000)
    )
    
    val growthScale = 1.0f + (petLevel.coerceIn(1, 10) * 0.05f)
    
    val autoRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse)
    )

    Box(
        modifier = modifier
            .background(dynamicBgColor)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onInteractStart() },
                    onDragEnd = { dragOffsetX = 0f; dragOffsetY = 0f; onInteractEnd() },
                    onDragCancel = { dragOffsetX = 0f; dragOffsetY = 0f; onInteractEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (petMood == PetMood.PETTING || petMood == PetMood.HAPPY) {
            val particleOffset by infiniteTransition.animateFloat(initialValue = 0f, targetValue = -150f, animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart))
            val particleAlpha by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 0f, animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart))
            Text("????", fontSize = 32.sp, modifier = Modifier.offset(y = particleOffset.dp).graphicsLayer { alpha = particleAlpha })
        }

        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val cameraNode = rememberCameraNode(engine) {
            position = Position(y = 0.5f, z = 4.0f)
        }
        
        val centerNode = remember {
            io.github.sceneview.node.Node(engine).apply {
                try {
                    val modelInstance = modelLoader.createModelInstance("models/pet_pig.glb")
                    if (modelInstance != null) {
                        val modelNode = ModelNode(
                            modelInstance = modelInstance,
                            scaleToUnits = 0.5f
                        ).apply {
                            position = Position(y = -0.3f)
                        }
                        addChildNode(modelNode)
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        Scene(
            modifier = Modifier
                .size(300.dp)
                .graphicsLayer {
                    scaleX = interactScale * growthScale
                    scaleY = interactScale * growthScale
                },
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            childNodes = listOf(centerNode),
            onFrame = {
                val rotY = animatedDragOffsetX * 0.5f + autoRotation + if (isSad) 180f else 0f
                val rotX = animatedDragOffsetY * 0.5f + if (isSad) 30f else 0f
                centerNode.rotation = Rotation(x = rotX, y = rotY, z = 0f)
            }
        )
    }
}

@Composable
fun MissionsDialog(
    viewModel: FinanceViewModel,
    isSpan: Boolean,
    onDismiss: () -> Unit
) {
    val currentAuditText by viewModel.currentAuditText.collectAsState()
    val missions by viewModel.missions.collectAsState()
    val isLoading by viewModel.isCoachingLoading.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isSpan) "Auditoría y Misiones (IA)" else "Audit & Missions (AI)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (isSpan) "Gemini está analizando tus finanzas..." else "Gemini is analyzing your finances...", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                } else {
                    if (currentAuditText != null) {
                        Text("Diagnóstico:", fontWeight = FontWeight.Bold)
                        Text(currentAuditText ?: "", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Text("Misiones Activas:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val activeMissions = missions.filter { !it.isCompleted }
                    if (activeMissions.isEmpty()) {
                        Text(if (isSpan) "No hay misiones activas." else "No active missions.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(activeMissions) { mission ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.width(200.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(mission.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(mission.description, fontSize = 12.sp, lineHeight = 16.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("+${mission.xpReward} XP", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { viewModel.completeMission(mission.id) },
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(if (isSpan) "Completar" else "Complete", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.requestAuditAndMissions() }) {
                Text(if (isSpan) "Generar Nuevas (IA)" else "Generate New (AI)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isSpan) "Cerrar" else "Close")
            }
        }
    )
}