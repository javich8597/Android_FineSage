package com.example.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.finance.FinanceViewModel

// --- NEW SIMULATOR COMPONENT ---
@Composable
fun SavingsGoalSimulator(labels: Map<String, String>, isDarkMode: Boolean) {
    var goalAmount by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(5000f) }
    var monthlyContribution by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(200f) }

    val monthsToReach = if (monthlyContribution > 0) (goalAmount / monthlyContribution).toInt() else 0
    val years = monthsToReach / 12
    val remainingMonths = monthsToReach % 12
    
    val isEsp = labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL"

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1A1C1E) else Color.White
        ),
        border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (isEsp) "Simulador de Meta de Ahorro" else "Savings Goal Simulator",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Goal Amount Slider
            Text(
                text = if (isEsp) "Meta: ${"%.0f".format(goalAmount)} €" else "Goal: ${"%.0f".format(goalAmount)} €",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDarkMode) Color(0xFFBAC3FF) else MaterialTheme.colorScheme.primary
            )
            Slider(
                value = goalAmount,
                onValueChange = { goalAmount = it },
                valueRange = 500f..50000f,
                steps = 99, // 100 intervals
                colors = SliderDefaults.colors(
                    thumbColor = if (isDarkMode) Color(0xFFBAC3FF) else MaterialTheme.colorScheme.primary,
                    activeTrackColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
                )
            )

            // Monthly Contribution Slider
            Text(
                text = if (isEsp) "Aportación mensual: ${"%.0f".format(monthlyContribution)} €" else "Monthly Contribution: ${"%.0f".format(monthlyContribution)} €",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDarkMode) Color(0xFFBAC3FF) else MaterialTheme.colorScheme.primary
            )
            Slider(
                value = monthlyContribution,
                onValueChange = { monthlyContribution = it },
                valueRange = 50f..5000f,
                steps = 99,
                colors = SliderDefaults.colors(
                    thumbColor = if (isDarkMode) Color(0xFFBAC3FF) else MaterialTheme.colorScheme.primary,
                    activeTrackColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDarkMode) Color(0xFF2C2F34) else Color(0xFFF0F2F6))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val timeString = if (years > 0) {
                    if (isEsp) "$years años y $remainingMonths meses" else "$years years and $remainingMonths months"
                } else {
                    if (isEsp) "$remainingMonths meses" else "$remainingMonths months"
                }

                Text(
                    text = if (isEsp) "Tiempo estimado: $timeString" else "Estimated Time: $timeString",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black
                )
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
