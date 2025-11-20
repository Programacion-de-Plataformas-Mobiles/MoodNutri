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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.moodnutri.data.models.openAi.GeneratedRecipe
import com.example.moodnutri.viewmodel.RecipeFinderState
import com.example.moodnutri.viewmodel.RecipeFinderViewModel
import com.google.gson.Gson
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    navController: NavController,
    ingredients: List<String>,
    mood: String,
    time: String,
    viewModel: RecipeFinderViewModel = hiltViewModel()
    ingredients: List<String> = emptyList(),
    mood: String = "",
    time: String = "",
    viewModel: RecipeFinderViewModel = viewModel()
) {
    LaunchedEffect(key1 = ingredients, key2 = mood, key3 = time) {
        if (ingredients.isNotEmpty()) {
            viewModel.searchRecipe(ingredients, mood, time)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    Scaffold(
        topBar = { SharedTopAppBar() },
        bottomBar = { SharedBottomNavigationBar(navController = navController, selectedTab = "Recipes") }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is RecipeFinderState.Idle -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 50.dp)) {
                        Text("Go to the Ingredients tab to start a new recipe search.")
                    }
                }
                is RecipeFinderState.Loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 50.dp)) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Finding the perfect recipe...")
                    }
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
                    GeneratedRecipeContent(recipe = state.recipe, navController = navController)
                }
                is RecipeFinderState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 50.dp)) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

/*
@Composable
fun GeneratedRecipeContent(recipe: GeneratedRecipe, navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            model = recipe.image_url,
            contentDescription = recipe.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(16.dp))
        Text(recipe.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Ready in: ${recipe.time}", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))

        Text("Why this recipe?", style = MaterialTheme.typography.titleMedium)
        Text(
            "\"${recipe.reason}\"",
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text("Ingredients:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        recipe.ingredients.forEach { Text("- $it", modifier = Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(24.dp))
        Text("Steps:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        recipe.steps.forEachIndexed { index, step -> Text("${index + 1}. $step", modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) }
        
        Spacer(Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { }) {
                Text("Save Recipe")
            }
            Button(onClick = { 
                val recipeJson = Gson().toJson(recipe)
                val encodedJson = URLEncoder.encode(recipeJson, "UTF-8")
                navController.navigate("scan_meal?baseRecipe=$encodedJson")
            }) {
                Text("Scan Meal")
            }
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeScreenPreview() {
    RecipeScreen(navController = rememberNavController(), ingredients = emptyList(), mood = "Happy", time = "30 min")
}
*/
