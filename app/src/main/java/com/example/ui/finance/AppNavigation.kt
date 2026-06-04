package com.example.ui.finance

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Coach : Screen("coach")
    object Goals : Screen("goals")
    object History : Screen("history")
    object Charts : Screen("charts")
    object Settings : Screen("settings")
}
