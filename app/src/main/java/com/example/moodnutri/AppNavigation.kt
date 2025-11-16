package com.example.moodnutri

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moodnutri.mockups.* 
import com.example.moodnutri.ui.RecipeScreen
import com.example.moodnutri.viewmodel.RecipeFinderViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Instanciamos los ViewModels aquí para que su estado sobreviva a la navegación
    val homeViewModel: HomeViewModel = viewModel()
    val recipeFinderViewModel: RecipeFinderViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController)
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
        // Nueva ruta estable para la pestaña "Recipes"
        composable("recipes_destination") { 
            RecipeScreen(navController = navController, viewModel = recipeFinderViewModel)
        }
        composable("profile") {
            ProfileScreen(navController = navController)
        }
    }
}
