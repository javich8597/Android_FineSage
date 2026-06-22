package com.example.ui.goals

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
fun GoalsTab(
    viewModel: FinanceViewModel,
    labels: Map<String, String>,
    isDarkMode: Boolean,
    onAddGoalClick: () -> Unit
) {
    val goalsList by viewModel.goals.collectAsState()
    var selectedGoalForFunds by remember { mutableStateOf<BudgetGoal?>(null) }
    var selectedGoalForOptions by remember { mutableStateOf<BudgetGoal?>(null) }

    if (selectedGoalForFunds != null) {
        AddFundsModal(
            goal = selectedGoalForFunds!!,
            onDismiss = { selectedGoalForFunds = null },
            onConfirm = { amount ->
                viewModel.addFundsToGoal(selectedGoalForFunds!!, amount)
                selectedGoalForFunds = null
            }
        )
    }

    if (selectedGoalForOptions != null) {
        Dialog(onDismissRequest = { selectedGoalForOptions = null }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Opciones del Objetivo", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selectedGoalForFunds = selectedGoalForOptions
                            selectedGoalForOptions = null
                        }
                    ) {
                        Text("Añadir Fondos")
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            viewModel.deleteGoal(selectedGoalForOptions!!)
                            selectedGoalForOptions = null
                        }
                    ) {
                        Text("Eliminar Objetivo")
                    }
                }
            }
        }
    }

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
                GoalListItem(
                    goal = goal,
                    isDarkMode = isDarkMode,
                    onClick = { selectedGoalForOptions = goal }
                )
            }
        }
    }
}

@Composable
fun GoalListItem(goal: BudgetGoal, isDarkMode: Boolean, onClick: () -> Unit = {}) {
    val progress = (goal.savedAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1A1C1E) else Color.White
        ),
        border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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

            if (goal.targetAmount > goal.savedAmount) {
                Spacer(modifier = Modifier.height(10.dp))
                var neededPerMonth: Double? = null
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val targetDate = sdf.parse(goal.targetDate)
                    if (targetDate != null) {
                        val diffInMillis = targetDate.time - System.currentTimeMillis()
                        val diffInMonths = (diffInMillis / (1000L * 60 * 60 * 24 * 30)).coerceAtLeast(1)
                        neededPerMonth = (goal.targetAmount - goal.savedAmount) / diffInMonths
                    }
                } catch (e: Exception) {
                    // Ignore date parsing errors
                }
                
                if (neededPerMonth != null) {
                    Text(
                        "Necesitas ahorrar aprox. ${"%.2f".format(neededPerMonth)} €/mes para llegar a tiempo.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

// --- TAB 4: MANAGEMENT AND EXPORT HUB ---
@Composable
fun AddFundsModal(
    goal: BudgetGoal,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountStr by remember { mutableStateOf(TextFieldValue("")) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Añadir Fondos a ${goal.title}", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Cantidad a añadir (€)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.text.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onConfirm(amt)
                            }
                        }
                    ) {
                        Text("Añadir")
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

