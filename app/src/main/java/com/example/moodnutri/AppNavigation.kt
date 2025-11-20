package com.example.moodnutri

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moodnutri.mockups.*
import com.example.moodnutri.viewmodel.AuthViewModel
import com.example.moodnutri.viewmodel.HomeRecipesViewModel
import com.example.moodnutri.viewmodel.ProfileViewModel
import com.example.moodnutri.viewmodel.RecipeFinderViewModel
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
                viewModel = homeViewModel,
                recipesViewModel = homeRecipesViewModel
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

        // Nueva ruta para mostrar receta guardada
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