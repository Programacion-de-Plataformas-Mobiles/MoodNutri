package com.example.moodnutri.presentation.screens.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.moodnutri.R
import com.example.moodnutri.data.models.openai.GeneratedRecipe
import com.example.moodnutri.presentation.components.SharedBottomNavigationBar
import com.example.moodnutri.presentation.components.SharedTopAppBar
import com.google.gson.Gson
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    navController: NavController,
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 50.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.select_image_prompt),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is RecipeFinderState.Loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 50.dp)
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.generating_recipe),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is RecipeFinderState.Success -> {
                    GeneratedRecipeContent(
                        recipe = state.recipe,
                        recipeId = state.recipeId,
                        navController = navController,
                        viewModel = viewModel
                    )
                }
                is RecipeFinderState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 50.dp)
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratedRecipeContent(
    recipe: GeneratedRecipe,
    recipeId: String,
    navController: NavController,
    viewModel: RecipeFinderViewModel
) {
    val isFavorite by viewModel.isFavorite.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val saveInProgress by viewModel.saveInProgress.collectAsState()
    val favoriteInProgress by viewModel.favoriteInProgress.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = recipe.image_url,
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            // Botón de favorito en la esquina superior derecha de la imagen
            IconButton(
                onClick = { viewModel.toggleFavorite() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                enabled = !favoriteInProgress
            ) {
                if (favoriteInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite)
                            stringResource(R.string.remove_from_favorites)
                        else
                            stringResource(R.string.add_to_favorites),
                        tint = if (isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = recipe.name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.ready_in, recipe.time),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.why_this_recipe),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "\"${recipe.reason}\"",
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(bottom = 24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = stringResource(R.string.ingredients_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        recipe.ingredients.forEach {
            Text(
                text = "- $it",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.steps_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        recipe.steps.forEachIndexed { index, step ->
            Text(
                text = "${index + 1}. $step",
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(32.dp))

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Botón Save
            Button(
                onClick = { viewModel.saveRecipe() },
                enabled = !saveInProgress && !isSaved,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) Color(0xFF2E7D32) else Color(0xFF4CAF50),
                    disabledContainerColor = Color(0xFF2E7D32)
                ),
                modifier = Modifier.weight(1f)
            ) {
                if (saveInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.saving))
                } else if (isSaved) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.saved),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.saved))
                } else {
                    Text(stringResource(R.string.save_recipe_button))
                }
            }

            Spacer(Modifier.width(12.dp))

            // Botón Scan Meal
            Button(
                onClick = {
                    val recipeJson = Gson().toJson(recipe)
                    val encodedJson = URLEncoder.encode(recipeJson, "UTF-8")
                    navController.navigate("scan_meal?baseRecipe=$encodedJson")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.scan_meal_button))
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeScreenPreview() {
    RecipeScreen(
        navController = rememberNavController(),
        ingredients = emptyList(),
        mood = "Happy",
        time = "30 min"
    )
}