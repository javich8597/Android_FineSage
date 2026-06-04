package com.example.ui.history

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
fun HistoryTab(viewModel: FinanceViewModel, labels: Map<String, String>, isDarkMode: Boolean) {
    val txState by viewModel.transactions.collectAsState()
    val categoryItems by viewModel.categoryItems.collectAsState()
    val rules by viewModel.rules.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedBankFilter by remember { mutableStateOf("Todos") }
    var selectedCategoryFilter by remember { mutableStateOf("Todos") }
    var selectedTypeFilter by remember { mutableStateOf("Todos") } // "Todos", "Gastos", "Ingresos"

    var showManageCategoriesDialog by remember { mutableStateOf(false) }
    var txToEditCategory by remember { mutableStateOf<Transaction?>(null) }

    // Filter transaction list sorted by timestamp DESC
    val filteredTx = remember(txState, searchQuery, selectedBankFilter, selectedCategoryFilter, selectedTypeFilter) {
        txState.filter { tx ->
            val matchesSearch = tx.concept.contains(searchQuery, ignoreCase = true) ||
                    tx.category.contains(searchQuery, ignoreCase = true) ||
                    tx.subcategory.contains(searchQuery, ignoreCase = true)
            
            val matchesBank = selectedBankFilter == "Todos" || tx.bankName == selectedBankFilter
            val matchesCat = selectedCategoryFilter == "Todos" || tx.category == selectedCategoryFilter
            val matchesType = when (selectedTypeFilter) {
                "Gastos" -> tx.amount < 0
                "Ingresos" -> tx.amount > 0
                else -> true
            }

            matchesSearch && matchesBank && matchesCat && matchesType
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Historial de Transacciones" else "Transaction History",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
            )
            val context = LocalContext.current
            val isSpanish = labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL"
            IconButton(
                onClick = { printMonthlyExpensePdf(context, txState, isSpanish) },
                modifier = Modifier.testTag("export_pdf_btn")
            ) {
                Icon(
                    Icons.Filled.Print,
                    contentDescription = "Export PDF",
                    tint = if (isDarkMode) Color.White else MaterialTheme.colorScheme.primary
                )
            }
        }

        // Primary Interactive Filter Buttons Row (Segmented style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDarkMode) Color(0xFF141618) else Color(0xFFECEFF1))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Todos", "Gastos", "Ingresos").forEach { type ->
                val isSelected = selectedTypeFilter == type
                val labelText = when (type) {
                    "Todos" -> if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Todo" else "All"
                    "Gastos" -> if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Gastos" else "Expenses"
                    "Ingresos" -> if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Ingresos" else "Income"
                    else -> type
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
                        .clickable { selectedTypeFilter = type }
                        .padding(vertical = 10.dp)
                        .testTag("history_type_btn_$type"),
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

        // Action Options ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showManageCategoriesDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Category, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Administrar Categorías" else "Manage Categories", fontSize = 12.sp)
            }
            
            // Show count of rules learned
            Text(
                text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "IA Aprendiendo: ${rules.size} reglas" else "AI Learning: ${rules.size} rules",
                fontSize = 11.sp,
                color = Color.Gray,
                fontStyle = FontStyle.Italic
            )
        }

        // Search & Filters Box
        var filtersExpanded by remember { mutableStateOf(false) }
        
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Search Input with Filter Toggle
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Buscar..." else "Search...") },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    
                    FilledIconButton(
                        onClick = { filtersExpanded = !filtersExpanded },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (filtersExpanded || selectedBankFilter != "Todos" || selectedCategoryFilter != "Todos") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            contentColor = if (filtersExpanded || selectedBankFilter != "Todos" || selectedCategoryFilter != "Todos") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filters")
                    }
                }

                AnimatedVisibility(visible = filtersExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Filter by Entity (BankName)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Filtrar por Entidad:" else "Filter by Entity:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Todos", "Revolut", "TradeRepublic", "Banco Tradicional", "Manual").forEach { bank ->
                                    val isSelected = selectedBankFilter == bank
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedBankFilter = bank },
                                        label = { Text(bank, fontSize = 11.sp) },
                                        isDarkMode = isDarkMode
                                    )
                                }
                            }
                        }

                        // Filter by Category
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Filtrar por Categoría:" else "Filter by Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val distinctCats = remember(categoryItems) {
                                    listOf("Todos") + categoryItems.map { it.category }.distinct()
                                }
                                distinctCats.forEach { cat ->
                                    val isSelected = selectedCategoryFilter == cat
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCategoryFilter = cat },
                                        label = { Text(cat, fontSize = 11.sp) },
                                        leadingIcon = if (cat != "Todos") {
                                            {
                                                Icon(
                                                    imageVector = com.example.ui.finance.getCategoryIcon(cat),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        } else null,
                                        isDarkMode = isDarkMode
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Transactions List View
        if (filteredTx.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "No se encontraron transacciones." else "No transactions match your search.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredTx.forEach { tx ->
                    TransactionHistoryItem(
                        tx = tx,
                        isDarkMode = isDarkMode,
                        labels = labels,
                        viewModel = viewModel,
                        onEditClick = { txToEditCategory = tx }
                    )
                }
            }
        }
    }

    // --- MANAGE CATEGORIES OVERLAY POPUP ---
    if (showManageCategoriesDialog) {
        ManageCategoriesModal(
            viewModel = viewModel,
            labels = labels,
            isDarkMode = isDarkMode,
            onDismiss = { showManageCategoriesDialog = false }
        )
    }

    // --- EDIT CATEGORIZATION DIALOG LINKED TO IA LEARNING ---
    if (txToEditCategory != null) {
        EditTransactionCategorizationModal(
            transaction = txToEditCategory!!,
            viewModel = viewModel,
            labels = labels,
            isDarkMode = isDarkMode,
            onDismiss = { txToEditCategory = null }
        )
    }
}

@Composable
fun TransactionHistoryItem(
    tx: Transaction,
    isDarkMode: Boolean,
    labels: Map<String, String>,
    viewModel: FinanceViewModel,
    onEditClick: () -> Unit
) {
    val selectCurrency by viewModel.selectedCurrency.collectAsState()
    val isExpense = tx.amount < 0
    val amountColor = if (isExpense) {
        if (isDarkMode) Color(0xFFBAC3FF) else Color(0xFFBA1A1A)
    } else {
        if (isDarkMode) Color(0xFFB4F1B6) else Color(0xFF006E1B)
    }
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateStr = formatter.format(Date(tx.timestamp))

    // Real-time currency conversion
    val convertedAmount = viewModel.convertCurrency(tx.amount, tx.currency, selectCurrency)
    val formattedAmount = viewModel.formatCurrency(convertedAmount, selectCurrency)
    val showOriginalSymbol = tx.currency != selectCurrency

    val categoryIcon = com.example.ui.finance.getCategoryIcon(tx.category)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E2125) else Color.White
        ),
        border = if (isDarkMode) BorderStroke(1.dp, Color(0xFF2D3135)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    // Category Icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color(0xFF2A2D32) else Color(0xFFF2F4F7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = tx.category,
                            tint = if (isDarkMode) Color.White else Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = tx.concept,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                        Text(text = dateStr, fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (!isExpense && convertedAmount > 0) "+" else ""}$formattedAmount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = amountColor
                    )
                    if (showOriginalSymbol) {
                        Text(
                            text = "(${if (!isExpense && tx.amount > 0) "+" else ""}${viewModel.formatCurrency(tx.amount, tx.currency)})",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Autonomous Category Tags
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDarkMode) Color(0xFF1B2B41) else Color(0xFFE5F1FD))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tx.category.uppercase(),
                            color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (tx.subcategory.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isDarkMode) Color(0xFF232D28) else Color(0xFFE6F5EA))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tx.subcategory.uppercase(),
                                color = if (isDarkMode) Color(0xFFB4F1B6) else Color(0xFF006E1B),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bank Identifier Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDarkMode) Color(0xFF28282B) else Color(0xFFECEFF1))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(text = tx.bankName, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }

                // Edit/Classify Trigger Icon
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit categorization",
                        tint = if (isDarkMode) Color(0xFFBAC3FF) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (tx.isAnomaly) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDarkMode) Color(0xFF351C1C) else Color(0xFFFEE8E8))
                        .padding(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, tint = Color.Red, modifier = Modifier.size(12.dp))
                        Text(tx.anomalyReason ?: "Anomaly detected", color = if (isDarkMode) Color(0xFFFFB4AB) else Color.Red, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EditTransactionCategorizationModal(
    transaction: Transaction,
    viewModel: FinanceViewModel,
    labels: Map<String, String>,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    val categoryItems by viewModel.categoryItems.collectAsState()
    
    // Set initial values
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var selectedSubcategory by remember { mutableStateOf(transaction.subcategory) }

    // Distinct list of categories
    val categories = remember(categoryItems) {
        categoryItems.map { it.category }.distinct().ifEmpty { listOf("Alimentos", "Suscripción", "Transporte", "Restaurantes", "Ocio", "Salud", "Ingreso", "Inversiones", "Manual") }
    }

    // Filter subcategories matching selected Category
    val availableSubcategories = remember(categoryItems, selectedCategory) {
        categoryItems.filter { it.category == selectedCategory }.map { it.subcategory }.distinct().ifEmpty { listOf("Otros") }
    }

    LaunchedEffect(selectedCategory) {
        if (selectedCategory != transaction.category) {
            selectedSubcategory = availableSubcategories.firstOrNull() ?: "Otros"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("edit_categorization_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Editar Categorización" else "Edit Categorization",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "\"${transaction.concept}\"",
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
                )

                // Select Category Dropdown Button Row
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Categoría Principal:" else "Category:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = com.example.ui.finance.getCategoryIcon(cat),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                }

                // Select Subcategory Dropdown Button Row
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Subcategoría:" else "Subcategory:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableSubcategories.forEach { sub ->
                            val isSelected = selectedSubcategory == sub
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSubcategory = sub },
                                label = { Text(sub, fontSize = 11.sp) },
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                }

                // UI Learning Cue Highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDarkMode) Color(0xFF232A35) else Color(0xFFE5F1FD))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Psychology,
                                contentDescription = null,
                                tint = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "FinSage Aprendizaje IA" else "FinSage AI Learning",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") {
                                "Al guardar, la aplicación aprenderá autónomamente de este ajuste. Todas tus transacciones históricas o futuras que contengan \"${transaction.concept}\" se clasificarán automáticamente como $selectedCategory > $selectedSubcategory."
                            } else {
                                "Once saved, FinSage learns from your action. All future & historic transactions containing \"${transaction.concept}\" will automatically map to $selectedCategory > $selectedSubcategory."
                            },
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(labels["cancel"] ?: "Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            viewModel.updateTransactionCategory(transaction, selectedCategory, selectedSubcategory)
                            onDismiss()
                        }
                    ) {
                        Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Guardar y Enseñar" else "Save & Train")
                    }
                }
            }
        }
    }
}

@Composable
fun ManageCategoriesModal(
    viewModel: FinanceViewModel,
    labels: Map<String, String>,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    val categoryItems by viewModel.categoryItems.collectAsState()

    var newCategory by remember { mutableStateOf(TextFieldValue("")) }
    var newSubcategory by remember { mutableStateOf(TextFieldValue("")) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(8.dp)
                .testTag("manage_categories_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Gestionar Categorías" else "Manage Categories",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") {
                        "Añada o elimine combinaciones de categoría y subcategoría para transacciones."
                    } else {
                        "Add or remove custom categories and subcategories that you want to classify transactions with."
                    },
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // Input form to add a combination
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF131517) else Color(0xFFF0F2F6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Añadir Nueva Categorización" else "Add New Category Mapping", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newCategory,
                                onValueChange = { newCategory = it },
                                placeholder = { Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Categoría" else "Category") },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 11.sp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newSubcategory,
                                onValueChange = { newSubcategory = it },
                                placeholder = { Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Subcateg." else "Subcategory") },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 11.sp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(
                            onClick = {
                                if (newCategory.text.trim().isNotEmpty() && newSubcategory.text.trim().isNotEmpty()) {
                                    viewModel.addCustomCategoryItem(newCategory.text, newSubcategory.text)
                                    newCategory = TextFieldValue("")
                                    newSubcategory = TextFieldValue("")
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Crear Categoría" else "Create Category", fontSize = 12.sp)
                        }
                    }
                }

                // Scrollable List of existing items
                Text(
                    text = if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Listado de Categorías de Transacción" else "Current Mappings",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categoryItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDarkMode) Color(0xFF22252A) else Color(0xFFF9F9FB))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.category, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(item.subcategory, fontSize = 11.sp, color = Color.Gray)
                            }
                            IconButton(
                                onClick = { viewModel.deleteCustomCategoryItem(item) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Cerrar" else "Close")
                    }
                }
            }
        }
    }
}

