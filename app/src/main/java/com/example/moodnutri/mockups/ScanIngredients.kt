package com.example.moodnutri.mockups

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.moodnutri.R
import com.example.moodnutri.viewmodel.RecipeFinderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanIngredientsScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    recipeViewModel: RecipeFinderViewModel,
    scanViewModel: ScanIngredientsViewModel = viewModel()
) {
    val uiState by scanViewModel.uiState.collectAsState()
    val selectedImage by scanViewModel.selectedImage
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let {
                scanViewModel.analyzeImage(it) { ingredients ->
                    homeViewModel.setIngredients(ingredients)
                }
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> if (isGranted) { cameraLauncher.launch(null) } }
    )

    Scaffold(
        topBar = { SharedTopAppBar() },
        bottomBar = { SharedBottomNavigationBar(navController = navController, selectedTab = "Ingredients") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (selectedImage != null) {
                ImageSelectionButton(selectedImage = selectedImage, buttonText = "", onClick = { /* No-op */ })
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (homeViewModel.userIngredients.isEmpty() && uiState !is ScanUiState.Loading) {
                if (selectedImage == null) {
                    ImageSelectionButton(
                        selectedImage = null,
                        buttonText = stringResource(R.string.scan_ingredients_button),
                        onClick = {
                            val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(null)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.select_image_prompt),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (uiState is ScanUiState.Loading) {
                CircularProgressIndicator()
                Text(
                    stringResource(R.string.analyzing_ingredients),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (homeViewModel.userIngredients.isNotEmpty()) {
                IngredientEditor(homeViewModel.userIngredients, homeViewModel)
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        recipeViewModel.searchRecipe(
                            userIngredients = homeViewModel.userIngredients,
                            mood = homeViewModel.mood,
                            availableTime = homeViewModel.cookingTime
                        )
                        navController.navigate("recipes_destination") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.generate_recipe_button), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        scanViewModel.clearScan()
                        homeViewModel.userIngredients.clear()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.scan_again_button))
                }
            }

            if (uiState is ScanUiState.Error) {
                (uiState as ScanUiState.Error).message.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        scanViewModel.clearScan()
                        homeViewModel.userIngredients.clear()
                    }) {
                        Text(stringResource(R.string.try_again_button))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientEditor(ingredients: List<String>, homeViewModel: HomeViewModel) {
    var newIngredient by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.your_ingredients),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
            items(ingredients) {
                InputChip(
                    selected = false,
                    onClick = { homeViewModel.removeIngredient(it) },
                    label = { Text(it) },
                    trailingIcon = { Icon(Icons.Default.Close, "Remove") },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newIngredient,
                onValueChange = { newIngredient = it },
                label = { Text(stringResource(R.string.add_ingredient_hint)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(
                onClick = {
                    if (newIngredient.isNotBlank()) {
                        homeViewModel.addIngredient(newIngredient)
                        newIngredient = ""
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Add, stringResource(R.string.add))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanIngredientsScreenPreview() {
    // Para el preview, usamos un navController y viewModels de prueba
    ScanIngredientsScreen(
        navController = rememberNavController(),
        homeViewModel = viewModel(),
        recipeViewModel = viewModel()
    )
}