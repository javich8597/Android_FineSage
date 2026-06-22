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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
    var appUnlockedViaBiometrics by remember { mutableStateOf(false) }

    // Setup active translation bundle
    val labels = remember(language) { Localization[language] ?: Localization["es"]!! }

    val navController = androidx.navigation.compose.rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    // Dialog state
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    // Use material background directly
    val backgroundColor = MaterialTheme.colorScheme.background
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(backgroundColor, MaterialTheme.colorScheme.surface)
    )

    val contentColor = MaterialTheme.colorScheme.onBackground

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            if (isUserAuthenticated && (!isBiometricsEnabled || appUnlockedViaBiometrics)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth().height(64.dp)
                    ) {
                        val navigateToRoute: (String) -> Unit = { route ->
                            navController.navigate(route) {
                                popUpTo(Screen.Dashboard.route) {
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
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_overview")
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Coach.route,
                            onClick = { navigateToRoute(Screen.Coach.route) },
                            icon = { Icon(Icons.Filled.Psychology, null) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_coach")
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Goals.route,
                            onClick = { navigateToRoute(Screen.Goals.route) },
                            icon = { Icon(Icons.Filled.Flag, null) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_goals")
                        )
                        // Empty space for floating action button in center
                        if (currentRoute == Screen.Dashboard.route) {
                            NavigationBarItem(
                                selected = false,
                                onClick = { },
                                icon = { },
                                alwaysShowLabel = false,
                                enabled = false
                            )
                        }
                        NavigationBarItem(
                            selected = currentRoute == Screen.History.route,
                            onClick = { navigateToRoute(Screen.History.route) },
                            icon = { Icon(Icons.Filled.History, null) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_history")
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Charts.route,
                            onClick = { navigateToRoute(Screen.Charts.route) },
                            icon = { Icon(Icons.Filled.BarChart, null) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_charts")
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Settings.route,
                            onClick = { navigateToRoute(Screen.Settings.route) },
                            icon = { Icon(Icons.Filled.Tune, null) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_settings")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isUserAuthenticated && (!isBiometricsEnabled || appUnlockedViaBiometrics) && currentRoute == Screen.Dashboard.route) {
                Box(
                    modifier = Modifier
                        .offset(y = 52.dp)
                        .testTag("add_trans_fab")
                        .size(68.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            ),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable(onClick = { showAddTxDialog = true }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = labels["add_tx"],
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
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
            // --- SECURITY LOCK OVERLAY (SUPABASE AUTH) ---
            if (!isUserAuthenticated) {
                com.example.ui.shared.AuthScreen(
                    viewModel = viewModel,
                    labels = labels,
                    isDarkMode = isDarkMode
                )
            } else if (isBiometricsEnabled && !appUnlockedViaBiometrics) {
                BiometricLockScreen(
                    labels = labels,
                    isDarkMode = isDarkMode,
                    onSuccess = {
                        appUnlockedViaBiometrics = true
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
                            onSyncClick = { bank -> viewModel.synchronizeBank(bank) },
                            onNavigateToRoute = { route -> navController.navigate(route) }
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
