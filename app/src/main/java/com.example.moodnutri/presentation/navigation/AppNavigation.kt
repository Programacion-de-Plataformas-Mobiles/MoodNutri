// File: presentation/navigation/AppNavigation.kt
package com.example.moodnutri.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moodnutri.presentation.screens.auth.AuthViewModel
import com.example.moodnutri.presentation.screens.auth.LoginScreen
import com.example.moodnutri.presentation.screens.auth.SignUpScreen
import com.example.moodnutri.presentation.screens.favorites.FavoritesScreen
import com.example.moodnutri.presentation.screens.home.HomeRecipesViewModel
import com.example.moodnutri.presentation.screens.home.HomeScreen
import com.example.moodnutri.presentation.screens.home.HomeViewModel
import com.example.moodnutri.presentation.screens.ingredients.ScanIngredientsScreen
import com.example.moodnutri.presentation.screens.meal.ScanMealScreen
import com.example.moodnutri.presentation.screens.profile.ProfileScreen
import com.example.moodnutri.presentation.screens.profile.ProfileViewModel
import com.example.moodnutri.presentation.screens.recipes.RecipeDetailScreen
import com.example.moodnutri.presentation.screens.recipes.RecipeFinderViewModel
import com.example.moodnutri.presentation.screens.recipes.RecipeScreen
import java.net.URLDecoder

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()
    val recipeFinderViewModel: RecipeFinderViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val homeRecipesViewModel: HomeRecipesViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("signup") {
            SignUpScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("home") {
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel
            )
        }
        composable("scan_ingredients") {
            ScanIngredientsScreen(
                navController = navController,
                homeViewModel = homeViewModel,
                recipeViewModel = recipeFinderViewModel
            )
        }
        composable(
            route = "scan_meal?baseRecipe={baseRecipe}",
            arguments = listOf(navArgument("baseRecipe") {
                type = NavType.StringType
                nullable = true
            })
        ) {
            val baseRecipeJson = it.arguments?.getString("baseRecipe")
            val decodedRecipeJson = baseRecipeJson?.let { json -> URLDecoder.decode(json, "UTF-8") }
            ScanMealScreen(navController = navController, baseRecipeJson = decodedRecipeJson)
        }
        composable(
            route = "recipes_destination",
        ) {
            RecipeScreen(navController = navController, viewModel = recipeFinderViewModel)
        }

        composable(
            route = "recipe_detail/{recipeId}",
            arguments = listOf(navArgument("recipeId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            RecipeDetailScreen(
                navController = navController,
                recipeId = recipeId
            )
        }

        composable("favorites") {
            FavoritesScreen(navController = navController)
        }
        composable("profile") {
            ProfileScreen(
                navController = navController,
                authViewModel = authViewModel,
                profileViewModel = profileViewModel
            )
        }
    }
}