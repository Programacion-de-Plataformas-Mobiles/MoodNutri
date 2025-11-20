package com.example.moodnutri.mockups

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodnutri.data.models.MealIngredient
import com.example.moodnutri.data.models.openAi.GeneratedRecipe
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanMealScreen(
    navController: NavController,
    baseRecipeJson: String?,
    viewModel: ScanMealViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedImage by viewModel.selectedImage
    val totalCalories by viewModel.totalCalories
    val isCalculatingCalories by viewModel.isCalculatingCalories
    val context = LocalContext.current

    val baseRecipe = remember(baseRecipeJson) {
        baseRecipeJson?.let { Gson().fromJson(it, GeneratedRecipe::class.java) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let {
                viewModel.analyzeMeal(it, baseRecipe)
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> if (isGranted) { cameraLauncher.launch(null) } }
    )

    Scaffold(
        topBar = { SharedTopAppBar() },
        bottomBar = { SharedBottomNavigationBar(navController = navController, selectedTab = "Scan Meal") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedImage == null) {
                val instructionText = if (baseRecipe != null) 
                    "Scan Your Cooked Meal (Comparing with ${baseRecipe.name})" 
                else 
                    "Scan Your Cooked Meal"
                
                ImageSelectionButton(selectedImage = null, buttonText = instructionText) {
                    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch(null)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            } else {
                ImageSelectionButton(selectedImage = selectedImage, buttonText = "") {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is ScanMealUiState.Loading -> {
                    CircularProgressIndicator()
                    Text("Analyzing meal...")
                }
                is ScanMealUiState.Success -> {
                    IngredientEditList(viewModel)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Sección de cálculo de calorías
                    if (totalCalories != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Total Calories", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "$totalCalories kcal", 
                                    style = MaterialTheme.typography.headlineMedium, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(
                        onClick = { viewModel.calculateCalories() },
                        enabled = !isCalculatingCalories,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isCalculatingCalories) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp), 
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calculating...")
                        } else {
                            Text("Calculate Total Calories")
                        }
                    }
                }
                is ScanMealUiState.Error -> {
                    Text((uiState as ScanMealUiState.Error).message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.clearScan() }) {
                        Text("Try Again")
                    }
                }
                is ScanMealUiState.Idle -> {
                    Text("Take a picture of your meal to analyze its ingredients.")
                }
            }
        }
    }
}

@Composable
fun IngredientEditList(viewModel: ScanMealViewModel) {
    var newIngredientName by remember { mutableStateOf("") }
    var newIngredientQuantity by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row {
            Text("Ingredient", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("Quantity", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(48.dp)) 
        }

        // Usamos el ID único para la key
        viewModel.detectedIngredients.forEachIndexed { index, ingredient ->
            EditableIngredientRow(ingredient, index, viewModel)
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        Text("Add New Ingredient:", fontWeight = FontWeight.SemiBold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newIngredientName,
                onValueChange = { newIngredientName = it },
                label = { Text("Name") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = newIngredientQuantity,
                onValueChange = { newIngredientQuantity = it },
                label = { Text("Qty") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                if (newIngredientName.isNotBlank()) {
                    viewModel.addIngredient(newIngredientName, newIngredientQuantity)
                    newIngredientName = ""
                    newIngredientQuantity = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}

@Composable
fun EditableIngredientRow(ingredient: MealIngredient, index: Int, viewModel: ScanMealViewModel) {
    // Aquí usamos key con ingredient.id para asegurar estabilidad y evitar recomposición total y pérdida de foco
    // al actualizar el objeto en la lista del viewModel.
    key(ingredient.id) {
        var name by remember { mutableStateOf(ingredient.name) }
        var quantity by remember { mutableStateOf(ingredient.quantity) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    viewModel.updateIngredient(index, it, quantity)
                },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = quantity,
                onValueChange = {
                    quantity = it
                    viewModel.updateIngredient(index, name, it)
                },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.removeIngredient(ingredient) }) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanMealScreenPreview() {
    ScanMealScreen(navController = rememberNavController(), baseRecipeJson = null)
}
