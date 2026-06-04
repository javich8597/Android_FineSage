package com.example.ui.finance

import com.example.ui.shared.*
import com.example.ui.dashboard.*
import com.example.ui.coach.*
import com.example.ui.goals.*
import com.example.ui.history.*
import com.example.ui.management.*
import com.example.ui.charts.*


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


import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination

@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun FinanceAppScreen(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val language by viewModel.language.collectAsState()
    val isBiometricsEnabled by viewModel.isBiometricsEnabled.collectAsState()
    val isUserAuthenticated by viewModel.isUserAuthenticated.collectAsState()

    // Setup active translation bundle
    val labels = remember(language) { Localization[language] ?: Localization["es"]!! }

    val navController = androidx.navigation.compose.rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    // Dialog state
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    // Custom background brush based on Selected theme (Sophisticated Dark vs Clean Dynamic Light)
    val backgroundBrush = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0F1113), Color(0xFF16191D))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFAFBFD), Color(0xFFEFEFF4))
        )
    }

    val contentColor = if (isDarkMode) Color(0xFFE2E2E6) else Color(0xFF151515)

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        bottomBar = {
            if (isUserAuthenticated) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDarkMode) Color(0xFF1A1C1E) else Color(0xFFF0F3F9))
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    if (isDarkMode) {
                        Divider(color = Color(0xFF2D3135), thickness = 1.dp)
                    }
                    NavigationBar(
                        containerColor = if (isDarkMode) Color(0xFF1A1C1E) else Color(0xFFF0F3F9),
                        tonalElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val navigateToRoute: (String) -> Unit = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }

                        NavigationBarItem(
                            selected = currentRoute == Screen.Dashboard.route,
                            onClick = { navigateToRoute(Screen.Dashboard.route) },
                            icon = { Icon(Icons.Filled.AccountBalanceWallet, null) },
                            label = { Text(labels["nav_resumen"] ?: "") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_item_overview")
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Coach.route,
                            onClick = { navigateToRoute(Screen.Coach.route) },
                            icon = { Icon(Icons.Filled.Psychology, null) },
                            label = { Text(labels["nav_coach"] ?: "") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_item_coach")
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Goals.route,
                            onClick = { navigateToRoute(Screen.Goals.route) },
                            icon = { Icon(Icons.Filled.Flag, null) },
                            label = { Text(labels["nav_metas"] ?: "") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_item_goals")
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.History.route,
                            onClick = { navigateToRoute(Screen.History.route) },
                            icon = { Icon(Icons.Filled.History, null) },
                            label = { Text(if (labels["app_tag"] == "COACH CON INTELIGENCIA ARTIFICIAL") "Historial" else "History") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_item_history")
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Charts.route,
                            onClick = { navigateToRoute(Screen.Charts.route) },
                            icon = { Icon(Icons.Filled.BarChart, null) },
                            label = { Text(labels["nav_charts"] ?: "") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_item_charts")
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Settings.route,
                            onClick = { navigateToRoute(Screen.Settings.route) },
                            icon = { Icon(Icons.Filled.Tune, null) },
                            label = { Text(labels["nav_config"] ?: "") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isDarkMode) Color(0xFF3F4759) else MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_item_settings")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isUserAuthenticated && currentRoute == Screen.Dashboard.route) {
                FloatingActionButton(
                    onClick = { showAddTxDialog = true },
                    containerColor = if (isDarkMode) Color(0xFFD1E4FF) else MaterialTheme.colorScheme.primary,
                    contentColor = if (isDarkMode) Color(0xFF003258) else Color.White,
                    modifier = Modifier
                        .testTag("add_trans_fab")
                        .padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = labels["add_tx"])
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .sophisticatedDotMesh(isDarkMode)
                .padding(paddingValues)
        ) {
            // --- SECURITY LOCK OVERLAY (BIOMETRIC) ---
            if (isBiometricsEnabled && !isUserAuthenticated) {
                BiometricLockScreen(
                    labels = labels,
                    isDarkMode = isDarkMode,
                    onSuccess = {
                        viewModel.setAuthenticated(true)
                        Toast.makeText(context, labels["bio_success"], Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // Main Application Contents
                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Dashboard.route) {
                        DashboardTab(
                            viewModel = viewModel,
                            labels = labels,
                            isDarkMode = isDarkMode,
                            onSyncClick = { bank -> viewModel.synchronizeBank(bank) }
                        )
                    }
                    composable(Screen.Coach.route) {
                        CoachTab(
                            viewModel = viewModel,
                            labels = labels,
                            isDarkMode = isDarkMode
                        )
                    }
                    composable(Screen.Goals.route) {
                        GoalsTab(
                            viewModel = viewModel,
                            labels = labels,
                            isDarkMode = isDarkMode,
                            onAddGoalClick = { showAddGoalDialog = true }
                        )
                    }
                    composable(Screen.History.route) {
                        HistoryTab(
                            viewModel = viewModel,
                            labels = labels,
                            isDarkMode = isDarkMode
                        )
                    }
                    composable(Screen.Charts.route) {
                        ChartsTab(
                            viewModel = viewModel,
                            labels = labels,
                            isDarkMode = isDarkMode
                        )
                    }
                    composable(Screen.Settings.route) {
                        ManagementTab(
                            viewModel = viewModel,
                            labels = labels,
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS FOR MUTATIONS ---
    if (showAddTxDialog) {
        AddTransactionModal(
            labels = labels,
            viewModel = viewModel,
            onDismiss = { showAddTxDialog = false },
            onConfirm = { concept, amount, category, bank, currency ->
                viewModel.addManualTransaction(concept, amount, category, "", bank, currency)
                showAddTxDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalModal(
            labels = labels,
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, target, date, category ->
                viewModel.addManualGoal(title, target, date, category)
                showAddGoalDialog = false
            }
        )
    }
}

// --- BIOMETRIC SECURITY COMPONENT ---
