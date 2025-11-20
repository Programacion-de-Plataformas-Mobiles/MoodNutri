package com.example.moodnutri.mockups

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodnutri.ui.GeneratedRecipeContent // Suponiendo que GeneratedRecipeContent está en el paquete ui
import com.example.moodnutri.viewmodel.RecipeFinderState
import com.example.moodnutri.viewmodel.RecipeFinderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    navController: NavController,
    ingredients: List<String>,
    mood: String,
    time: String,
    viewModel: RecipeFinderViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = ingredients) {
        if (ingredients.isNotEmpty()) {
            // Ahora usamos los valores reales que vienen de la navegación
            viewModel.searchRecipe(ingredients, mood, time)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    Scaffold(
        topBar = { SharedTopAppBar() },
        bottomBar = { SharedBottomNavigationBar(navController = navController, selectedTab = "Recipes") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is RecipeFinderState.Idle -> {
                    if (ingredients.isNotEmpty()) {
                        // Si hay ingredientes, mostramos el loading por defecto
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Finding the perfect recipe...")
                    } else {
                        Text("Select ingredients to generate a recipe.")
                    }
                }
                is RecipeFinderState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Finding the perfect recipe...")
                }
                is RecipeFinderState.Success -> {
                    GeneratedRecipeContent(recipe = state.recipe)
                    IconButton(onClick = { viewModel.addOrRemoveFromFavorites(state.recipe, !isFavorite) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Add to favorites",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                }
                is RecipeFinderState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun RecipeScreenPreview() {
    RecipeScreen(navController = rememberNavController(), ingredients = emptyList(), mood = "Happy", time = "30 min")
}
*/
