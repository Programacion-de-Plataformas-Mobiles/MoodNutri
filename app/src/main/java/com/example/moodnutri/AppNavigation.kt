package com.example.moodnutri

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moodnutri.mockups.* 
import com.example.moodnutri.ui.RecipeScreen
import com.example.moodnutri.ui.auth.SignUpScreen
import com.example.moodnutri.ui.favorites.FavoritesScreen
import com.example.moodnutri.viewmodel.AuthViewModel
import com.example.moodnutri.viewmodel.RecipeFinderViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()
    val recipeFinderViewModel: RecipeFinderViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("signup") {
            SignUpScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController, viewModel = homeViewModel)
        }
        composable("scan_ingredients") {
            ScanIngredientsScreen(
                navController = navController, 
                homeViewModel = homeViewModel,
                recipeViewModel = recipeFinderViewModel // Pasamos el ViewModel de recetas
            )
        }
        composable("scan_meal") {
            ScanMealScreen(navController = navController)
        }
        composable("recipes_destination") { 
            RecipeScreen(navController = navController, viewModel = recipeFinderViewModel)
        }
        composable("favorites") {
            FavoritesScreen(navController = navController)
        }
        composable("profile") {
            ProfileScreen(navController = navController)
        }
    }
}
