package com.example.moodnutri.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
// import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.moodnutri.data.models.openAi.GeneratedRecipe
import com.example.moodnutri.mockups.SharedBottomNavigationBar
import com.example.moodnutri.mockups.SharedTopAppBar
import com.example.moodnutri.viewmodel.RecipeFinderState
import com.example.moodnutri.viewmodel.RecipeFinderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    navController: NavController,
    viewModel: RecipeFinderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { SharedTopAppBar() },
        // Ahora esta es oficialmente la pestaña "Recipes"
        bottomBar = { SharedBottomNavigationBar(navController = navController, selectedTab = "Recipes") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is RecipeFinderState.Idle -> {
                    Text("Go to the Ingredients tab to start a new recipe search.")
                }
                is RecipeFinderState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Finding the perfect recipe...")
                }
                is RecipeFinderState.Success -> {
                    GeneratedRecipeContent(recipe = state.recipe)
                }
                is RecipeFinderState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun GeneratedRecipeContent(recipe: GeneratedRecipe) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
        Button(onClick = { /* Placeholder */ }) {
            Text("Save Recipe")
        }

        // BOTÓN DE FAVORITOS AÑADIDO (sin tocar nada más arriba)
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        // TODO: Reemplazar con inyección de Hilt cuando esté disponible
        // val repository = remember { OfflineFavoritesRepository(context) }
        var isFavorite by remember { mutableStateOf(false) }

        LaunchedEffect(recipe.name) {
            // TODO: Implementar la verificación de favoritos
            // isFavorite = repository.isFavorite(recipe.name)
        }

        IconButton(
            onClick = {
                scope.launch {
                    if (isFavorite) {
                        // TODO: repository.removeFromFavorites(recipe.name)
                    } else {
                        // TODO: repository.addToFavorites(recipe)
                    }
                    isFavorite = !isFavorite
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color.Red else Color.Gray
            )
        }
    }
}

/*
// El preview necesita una instancia del ViewModel
@Preview(showBackground = true)
@Composable
fun RecipeScreenPreview() {
    RecipeScreen(navController = rememberNavController(), viewModel = RecipeFinderViewModel())
}
*/