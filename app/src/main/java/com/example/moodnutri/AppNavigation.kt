package com.example.moodnutri

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moodnutri.mockups.* 

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("scan_ingredients") {
            ScanIngredientsScreen(navController = navController)
        }
        composable("scan_meal") {
            ScanMealScreen(navController = navController)
        }
        composable("recipe") {
            RecipeScreen(navController = navController)
        }
        composable("profile") {
            ProfileScreen(navController = navController)
        }
    }
}
