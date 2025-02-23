package com.azcode.busmanagmentsystem.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.azcode.busmanagmentsystem.presentation.auth.ui.AuthScreen


@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "auth" // ✅ Ensure this is set correctly
    ) {
        composable("auth") { AuthScreen() }
    }
}

