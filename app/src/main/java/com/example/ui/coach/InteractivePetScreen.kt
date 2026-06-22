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

        // --- 1. 3D PET IN THE MIDDLE ---
        AnimatedLivingPet(
            petStyle = petStyle, 
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

@android.annotation.SuppressLint("SetJavaScriptEnabled")
@Composable
fun AnimatedLivingPet(
    petStyle: String, 
    petMood: PetMood, 
    mainColor: Color, 
    modifier: Modifier = Modifier.fillMaxSize(),
    onInteractStart: () -> Unit,
    onInteractEnd: () -> Unit
) {
    val context = LocalContext.current
    var isLoaded by remember { mutableStateOf(false) }

    val htmlContent = remember {
        """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, maximum-scale=1.0">
        <style>
            body { margin: 0; overflow: hidden; background-color: transparent; }
            canvas { display: block; width: 100vw; height: 100vh; outline: none; -webkit-tap-highlight-color: transparent; }
            #bg-gradient {
                position: absolute; top:0; left:0; width:100vw; height:100vh; z-index:-1;
                transition: background 1.5s ease;
                background: transparent;
            }
        </style>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
        </head>
        <body>
        <div id="bg-gradient"></div>
        <script>
            let scene, camera, renderer, activeObject, leaves = [], fallingLeaves = [];
            let currentStyle = '';
            let currentMood = '';
            let leafMaterial, barkMaterial, coreMaterial, blobMaterial;
            let ambientLight, dirLight;
            const raycaster = new THREE.Raycaster();
            const mouse = new THREE.Vector2();

            // Rotation state
            let targetRotationY = 0;
            let currentRotationY = 0;

            function init() {
                scene = new THREE.Scene();
                
                ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
                scene.add(ambientLight);
                
                dirLight = new THREE.DirectionalLight(0xfff5e6, 0.8);
                dirLight.position.set(10, 20, 15);
                dirLight.castShadow = true;
                scene.add(dirLight);

                const backLight = new THREE.DirectionalLight(0xaabbff, 0.3);
                backLight.position.set(-10, 10, -10);
                scene.add(backLight);

                camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 1000);
                camera.position.set(0, 5, 20);

                renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
                renderer.setSize(window.innerWidth, window.innerHeight);
                renderer.setPixelRatio(window.devicePixelRatio);
                renderer.shadowMap.enabled = true;
                renderer.shadowMap.type = THREE.PCFSoftShadowMap;
                document.body.appendChild(renderer.domElement);

                activeObject = new THREE.Group();
                scene.add(activeObject);

                setupEvents();
                animate();
            }

            function createBarkTexture() {
                const canvas = document.createElement('canvas');
                canvas.width = 256; canvas.height = 256;
                const ctx = canvas.getContext('2d');
                ctx.fillStyle = '#4a3b2c';
                ctx.fillRect(0,0,256,256);
                for(let i=0; i<500; i++){
                    ctx.fillStyle = Math.random() > 0.5 ? '#3a2b1c' : '#5a4b3c';
                    ctx.fillRect(Math.random()*256, Math.random()*256, Math.random()*20, Math.random()*3);
                }
                const tex = new THREE.CanvasTexture(canvas);
                tex.wrapS = THREE.RepeatWrapping;
                tex.wrapT = THREE.RepeatWrapping;
                return tex;
            }

            function buildTree() {
                const group = new THREE.Group();
                
                barkMaterial = new THREE.MeshStandardMaterial({ 
                    color: 0x5c4033, 
                    roughness: 0.9,
                    map: createBarkTexture()
                });
                
                leafMaterial = new THREE.MeshStandardMaterial({ 
                    color: 0x3b7a57, 
                    roughness: 0.6,
                    side: THREE.DoubleSide
                });

                leaves = [];
                
                const leafGeo = new THREE.PlaneGeometry(1.0, 1.0);
                leafGeo.translate(0, 0.5, 0);

                // Procedural generation representing a Bonsai
                function recurse(g, length, radius, depth) {
                    const branch = new THREE.Mesh(
                        new THREE.CylinderGeometry(radius*0.65, radius, length, 8),
                        barkMaterial
                    );
                    branch.position.y = length / 2;
                    branch.castShadow = true;
                    branch.receiveShadow = true;
                    g.add(branch);

                    const endPoint = new THREE.Group();
                    endPoint.position.y = length;
                    g.add(endPoint);

                    if (depth === 0) {
                        for(let i=0; i<8; i++) {
                            const leaf = new THREE.Mesh(leafGeo, leafMaterial);
                            leaf.rotation.set(Math.random()*Math.PI, Math.random()*Math.PI, Math.random()*Math.PI);
                            leaf.position.set((Math.random()-0.5)*1.5, (Math.random()-0.5)*1.5, (Math.random()-0.5)*1.5);
                            leaf.scale.setScalar(1.5 + Math.random());
                            leaf.castShadow = true;
                            endPoint.add(leaf);
                            leaves.push(leaf);
                        }
                        return;
                    }

                    const numBranches = (depth === 5) ? 2 : (depth >= 3 ? 3 : 2); 
                    for(let i=0; i<numBranches; i++) {
                        const newGrp = new THREE.Group();
                        endPoint.add(newGrp);
                        
                        const angleY = (Math.PI * 2 / numBranches) * i + (Math.random()-0.5)*0.8;
                        const angleX = 0.3 + Math.random() * 0.5;
                        
                        newGrp.rotation.y = angleY;
                        newGrp.rotation.x = angleX;
                        
                        recurse(newGrp, length * (0.65 + Math.random()*0.15), radius * 0.7, depth - 1);
                    }
                }
                
                const rootGroup = new THREE.Group();
                recurse(rootGroup, 3.5, 0.7, 5);
                group.add(rootGroup);
                
                const ground = new THREE.Mesh(
                    new THREE.CylinderGeometry(4.5, 5, 0.8, 24),
                    new THREE.MeshStandardMaterial({color: 0x2e3b22, roughness: 1.0})
                );
                ground.position.y = -0.4;
                ground.receiveShadow = true;
                group.add(ground);

                group.position.y = -2;
                return group;
            }

            function buildFuturistic() {
                const group = new THREE.Group();
                coreMaterial = new THREE.MeshStandardMaterial({ 
                    color: 0x00ffff, 
                    emissive: 0x00aaff, 
                    emissiveIntensity: 0.8,
                    wireframe: true 
                });
                const core = new THREE.Mesh(new THREE.IcosahedronGeometry(2.5, 2), coreMaterial);
                group.add(core);

                const ringMat = new THREE.MeshBasicMaterial({ color: 0x00ffff, side: THREE.DoubleSide, transparent: true, opacity: 0.4 });
                for(let i=0; i<3; i++) {
                    const ring = new THREE.Mesh(new THREE.TorusGeometry(4 + i*1.2, 0.05, 16, 100), ringMat);
                    ring.rotation.x = Math.random() * Math.PI;
                    ring.rotation.y = Math.random() * Math.PI;
                    ring.userData = { speedX: (Math.random()-0.5)*0.02, speedY: (Math.random()-0.5)*0.02 };
                    group.add(ring);
                }
                return group;
            }

            function buildTraditional() {
                const group = new THREE.Group();
                blobMaterial = new THREE.MeshStandardMaterial({ color: 0xffb6c1, roughness: 0.4 });
                const geo = new THREE.SphereGeometry(3.5, 32, 32);
                const pos = geo.getAttribute('position');
                for (let i = 0; i < pos.count; i++) {
                    let y = pos.getY(i);
                    if (y < 0) {
                        pos.setY(i, y * 0.5); // flatten bottom
                        pos.setX(i, pos.getX(i) * 1.2); pos.setZ(i, pos.getZ(i) * 1.2);
                    } else if (y > 2) {
                        pos.setY(i, y * 1.3); // elongate top
                    }
                }
                geo.computeVertexNormals();
                const blob = new THREE.Mesh(geo, blobMaterial);
                blob.castShadow = true;
                blob.position.y = -1;
                group.add(blob);
                
                const eyeMat = new THREE.MeshBasicMaterial({ color: 0x111111 });
                const eyeG = new THREE.SphereGeometry(0.35, 16, 16);
                const eyeL = new THREE.Mesh(eyeG, eyeMat); eyeL.position.set(-1.0, 1.0, 3.2);
                const eyeR = eyeL.clone(); eyeR.position.set(1.0, 1.0, 3.2);
                group.add(eyeL); group.add(eyeR);

                const mouth = new THREE.Mesh(new THREE.TorusGeometry(0.4, 0.08, 8, 16, Math.PI), eyeMat);
                mouth.position.set(0, -0.2, 3.4); mouth.rotation.x = Math.PI;
                group.add(mouth);

                return group;
            }

            function setStyle(style) {
                if (currentStyle === style) return;
                currentStyle = style;
                
                scene.remove(activeObject);
                if (style === 'ZEN') activeObject = buildTree();
                else if (style === 'FUTURISTIC') activeObject = buildFuturistic();
                else activeObject = buildTraditional();
                
                scene.add(activeObject);
                setMood(currentMood, true);
            }

            function setMood(mood, force = false) {
                if (currentMood === mood && !force) return;
                currentMood = mood;
                
                const bg = document.getElementById('bg-gradient');
                
                if (currentStyle === 'ZEN') {
                    if (mood === 'HAPPY' || mood === 'PETTING') {
                        bg.style.background = 'radial-gradient(circle at top, #87CEEB 0%, #e0f6ff 100%)';
                        dirLight.color.setHex(0xffffff); ambientLight.intensity = 0.8;
                        if(leafMaterial) leafMaterial.color.setHex(0x3b7a57); 
                    } else if (mood === 'IDLE') {
                        bg.style.background = 'radial-gradient(circle at top, #F4A460 0%, #ffe4b5 100%)';
                        dirLight.color.setHex(0xffddaa); ambientLight.intensity = 0.6;
                        if(leafMaterial) leafMaterial.color.setHex(0x8f9779); 
                    } else { // ANGRY 
                        bg.style.background = 'radial-gradient(circle at top, #4a5568 0%, #2d3748 100%)';
                        dirLight.color.setHex(0xaaaaaa); ambientLight.intensity = 0.3;
                        if(leafMaterial) leafMaterial.color.setHex(0x6b5e53); 
                    }
                } else if (currentStyle === 'FUTURISTIC') {
                    bg.style.background = 'transparent';
                    let c = 0x00ffff;
                    if(mood === 'HAPPY' || mood === 'PETTING') c = 0x00ff00;
                    else if(mood === 'ANGRY') c = 0xff0000;
                    if(coreMaterial) { coreMaterial.color.setHex(c); coreMaterial.emissive.setHex(c); }
                } else {
                    bg.style.background = 'transparent';
                    let c = 0xffb6c1;
                    if(mood === 'HAPPY' || mood === 'PETTING') c = 0xff69b4;
                    else if(mood === 'ANGRY') c = 0xcd5c5c;
                    if(blobMaterial) blobMaterial.color.setHex(c);
                }
                
                if(activeObject && currentStyle !== 'ZEN') {
                    const s = mood === 'PETTING' ? 1.1 : 1.0;
                    activeObject.scale.set(s, s, s);
                }
            }

            function spawnFallingLeaf(intersectPoint) {
                if(currentStyle !== 'ZEN' || fallingLeaves.length > 50) return;
                const leaf = new THREE.Mesh(
                    new THREE.PlaneGeometry(0.3, 0.3),
                    new THREE.MeshBasicMaterial({ color: leafMaterial.color, side: THREE.DoubleSide })
                );
                leaf.position.copy(intersectPoint);
                leaf.position.x += (Math.random() - 0.5);
                leaf.position.y += (Math.random() - 0.5);
                leaf.position.z += (Math.random() - 0.5);
                leaf.rotation.set(Math.random()*Math.PI, Math.random()*Math.PI, Math.random()*Math.PI);
                
                scene.add(leaf);
                fallingLeaves.push({
                    mesh: leaf,
                    vy: -0.015 - Math.random()*0.02,
                    vx: (Math.random()-0.5)*0.03,
                    vz: (Math.random()-0.5)*0.03,
                    rx: Math.random()*0.1,
                    ry: Math.random()*0.1,
                    life: 2.5
                });
            }

            function setupEvents() {
                let isPointerDown = false;
                let lastPointer = {x:0, y:0};
                let isScrolling = false;

                const parseEvent = (e) => {
                    if(e.touches && e.touches.length > 0) return { x: e.touches[0].clientX, y: e.touches[0].clientY };
                    return { x: e.clientX, y: e.clientY };
                };

                const onDown = (e) => {
                    isPointerDown = true;
                    isScrolling = false;
                    lastPointer = parseEvent(e);
                    try { window.AndroidInterface.onInteractionStart(); } catch(err){}
                };

                const onMove = (e) => {
                    if(!isPointerDown) return;
                    const p = parseEvent(e);
                    const dx = p.x - lastPointer.x;
                    const dy = p.y - lastPointer.y;
                    
                    // If moving vertically mostly, native scroll should take over, don't stop it.
                    if (Math.abs(dy) > Math.abs(dx)) {
                        isScrolling = true;
                    }

                    if (!isScrolling) {
                        targetRotationY += dx * 0.01;
                    }
                    
                    if(currentStyle === 'ZEN') {
                        mouse.x = (p.x / window.innerWidth) * 2 - 1;
                        mouse.y = -(p.y / window.innerHeight) * 2 + 1;
                        raycaster.setFromCamera(mouse, camera);
                        const intersects = raycaster.intersectObject(activeObject, true);
                        if(intersects.length > 0 && Math.random() > 0.4) {
                            spawnFallingLeaf(intersects[0].point);
                        }
                    }
                    lastPointer = p;
                };

                const onUp = () => {
                    isPointerDown = false;
                    try { window.AndroidInterface.onInteractionEnd(); } catch(err){}
                };

                const canvas = renderer.domElement;
                canvas.addEventListener('touchstart', onDown, {passive: true});
                canvas.addEventListener('touchmove', onMove, {passive: true});
                canvas.addEventListener('touchend', onUp, {passive: true});
                canvas.addEventListener('touchcancel', onUp, {passive: true});
                
                canvas.addEventListener('mousedown', onDown);
                window.addEventListener('mousemove', onMove);
                window.addEventListener('mouseup', onUp);
                
                window.addEventListener('resize', () => {
                    if(camera && renderer) {
                        camera.aspect = window.innerWidth / window.innerHeight;
                        camera.updateProjectionMatrix();
                        renderer.setSize(window.innerWidth, window.innerHeight);
                    }
                });
            }

            const clock = new THREE.Clock();
            function animate() {
                requestAnimationFrame(animate);
                const delta = clock.getDelta();
                const time = clock.getElapsedTime();

                // Smooth rotation interpolation
                currentRotationY += (targetRotationY - currentRotationY) * 0.1;
                if(activeObject) {
                    activeObject.rotation.y = currentRotationY;
                }

                if (currentStyle === 'ZEN' && activeObject && activeObject.children.length > 0) {
                    // Gentle wind swaying the tree slightly
                    activeObject.children[0].rotation.z = Math.sin(time * 0.5) * 0.015;
                    activeObject.children[0].rotation.x = Math.cos(time * 0.4) * 0.015;
                } else if (currentStyle === 'FUTURISTIC' && activeObject) {
                    activeObject.position.y = Math.sin(time * 2) * 0.5;
                    for(let i=1; i<activeObject.children.length; i++) {
                        const ring = activeObject.children[i];
                        ring.rotation.x += ring.userData.speedX;
                        // ring rotation relative to object rotation
                        ring.rotation.y += ring.userData.speedY; 
                    }
                } else if (currentStyle === 'TRADITIONAL' && activeObject) {
                    if(currentMood !== 'ANGRY') {
                        activeObject.children[0].scale.y = 1 + Math.sin(time * 4) * 0.05;
                    }
                }

                // Update particles
                for(let i=fallingLeaves.length-1; i>=0; i--) {
                    const l = fallingLeaves[i];
                    l.mesh.position.y += l.vy;
                    l.mesh.position.x += l.vx + Math.sin(time*3)*0.01;
                    l.mesh.position.z += l.vz + Math.cos(time*2)*0.01;
                    l.mesh.rotation.x += l.rx;
                    l.mesh.rotation.y += l.ry;
                    l.life -= delta;
                    if(l.life <= 0 || l.mesh.position.y < -5) {
                        scene.remove(l.mesh);
                        fallingLeaves.splice(i, 1);
                    }
                }

                renderer.render(scene, camera);
            }

            try { window.onload = init; } catch(e) {}
            window.setStyle = setStyle;
            window.setMood = setMood;
        </script>
        </body>
        </html>
        """.trimIndent()
    }

    val dynamicBgColor by androidx.compose.animation.animateColorAsState(
        targetValue = when (petMood) {
            PetMood.ANGRY -> Color.DarkGray.copy(alpha = 0.3f)
            PetMood.HAPPY -> mainColor.copy(alpha = 0.15f)
            PetMood.PETTING -> mainColor.copy(alpha = 0.2f)
            else -> Color.Transparent
        }, 
        animationSpec = androidx.compose.animation.core.tween(1000)
    )

    Box(
        modifier = modifier.background(dynamicBgColor),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { ctx ->
                android.webkit.WebView(ctx).apply {
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webChromeClient = android.webkit.WebChromeClient()
                    
                    addJavascriptInterface(object : Any() {
                        @android.webkit.JavascriptInterface
                        fun onInteractionStart() {
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch { onInteractStart() }
                        }
                        @android.webkit.JavascriptInterface
                        fun onInteractionEnd() {
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch { onInteractEnd() }
                        }
                    }, "AndroidInterface")
                    
                    webViewClient = object : android.webkit.WebViewClient() {
                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                            isLoaded = true
                            view?.evaluateJavascript("window.setStyle('$petStyle'); window.setMood('${petMood.name}');", null)
                        }
                    }
                    
                    loadDataWithBaseURL("https://threejs.org", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                if (isLoaded) {
                    webView.evaluateJavascript("window.setStyle('$petStyle'); window.setMood('${petMood.name}');", null)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}


