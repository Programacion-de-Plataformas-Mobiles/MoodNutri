package com.example.moodnutri.presentation.screens.meal

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodnutri.R
import com.example.moodnutri.data.models.MealIngredient
import com.example.moodnutri.data.models.openai.GeneratedRecipe
import com.example.moodnutri.presentation.components.ImageSelectionButton
import com.example.moodnutri.presentation.components.SharedBottomNavigationBar
import com.example.moodnutri.presentation.components.SharedTopAppBar
import com.example.moodnutri.presentation.screens.meal.ScanMealState
import com.google.gson.Gson
import kotlin.collections.forEachIndexed
import kotlin.collections.isNotEmpty
import kotlin.jvm.java
import kotlin.let
import kotlin.text.ifBlank
import kotlin.text.isNotBlank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanMealScreen(
    navController: NavController,
    baseRecipeJson: String?,
    viewModel: ScanMealViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedImage by viewModel.selectedImage
    val nutrition by viewModel.nutritionInfo.collectAsState()
    val isCalculatingNutrition by viewModel.isCalculatingNutrition
    val context = LocalContext.current

    var showSuccessDialog by remember { mutableStateOf(false) }

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
                    stringResource(R.string.scan_meal_comparing, baseRecipe.name)
                else
                    stringResource(R.string.scan_meal_title)

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
                is ScanMealState.Loading -> {
                    CircularProgressIndicator()
                    Text(
                        stringResource(R.string.analyzing_meal),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                is ScanMealState.Success -> {
                    IngredientEditList(viewModel)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón para calcular nutrición
                    Button(
                        onClick = { viewModel.calculateNutrition() },
                        enabled = !isCalculatingNutrition && viewModel.detectedIngredients.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isCalculatingNutrition) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.calculating))
                        } else {
                            Text(stringResource(R.string.calculate_nutrition_button))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sección de información nutricional
                    if (nutrition.calories > 0) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    stringResource(R.string.nutritional_information),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    NutritionItem(
                                        label = stringResource(R.string.calories_label),
                                        value = "${nutrition.calories}",
                                        unit = stringResource(R.string.kcal),
                                        icon = Icons.Default.LocalFireDepartment,
                                        color = Color(0xFFFF6B6B)
                                    )
                                    NutritionItem(
                                        label = stringResource(R.string.protein_label),
                                        value = "${nutrition.protein}",
                                        unit = stringResource(R.string.grams),
                                        icon = Icons.Default.FitnessCenter,
                                        color = Color(0xFF4ECDC4)
                                    )
                                    NutritionItem(
                                        label = stringResource(R.string.carbs_label),
                                        value = "${nutrition.carbs}",
                                        unit = stringResource(R.string.grams),
                                        icon = Icons.Default.Grain,
                                        color = Color(0xFFFFD93D)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón para agregar a comidas del día
                        Button(
                            onClick = {
                                viewModel.addMealToToday()
                                showSuccessDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Restaurant, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.add_to_meals_button))
                        }
                    }
                }
                is ScanMealState.Error -> {
                    Text(
                        (uiState as ScanMealState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.clearScan() }) {
                        Text(stringResource(R.string.try_again_button))
                    }
                }
                is ScanMealState.Idle -> {
                    Text(
                        stringResource(R.string.take_picture_prompt),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // Diálogo de confirmación
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.meal_added_title), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(stringResource(R.string.meal_added_message))
            },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.navigate("profile")
                }) {
                    Text(stringResource(R.string.view_profile_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text(stringResource(R.string.ok_button))
                }
            }
        )
    }
}

@Composable
fun IngredientEditList(viewModel: ScanMealViewModel) {
    var newIngredientName by remember { mutableStateOf("") }
    var newIngredientQuantity by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.detected_ingredients),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Lista de ingredientes detectados
        viewModel.detectedIngredients.forEachIndexed { index, ingredient ->
            EditableIngredientRow(ingredient, index, viewModel)
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Text(
            stringResource(R.string.add_new_ingredient),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newIngredientName,
                onValueChange = { newIngredientName = it },
                label = { Text(stringResource(R.string.ingredient_name)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = newIngredientQuantity,
                onValueChange = { newIngredientQuantity = it },
                label = { Text(stringResource(R.string.ingredient_quantity)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(
                onClick = {
                    if (newIngredientName.isNotBlank()) {
                        viewModel.addIngredient(newIngredientName, newIngredientQuantity.ifBlank { "1 unit" })
                        newIngredientName = ""
                        newIngredientQuantity = ""
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

@Composable
fun EditableIngredientRow(ingredient: MealIngredient, index: Int, viewModel: ScanMealViewModel) {
    key(ingredient.id) {
        var name by remember { mutableStateOf(ingredient.name) }
        var quantity by remember { mutableStateOf(ingredient.quantity) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        viewModel.updateIngredient(index, it, quantity)
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(stringResource(R.string.ingredient), fontSize = 12.sp) }
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                        viewModel.updateIngredient(index, name, it)
                    },
                    modifier = Modifier.weight(0.7f),
                    singleLine = true,
                    label = { Text(stringResource(R.string.quantity), fontSize = 12.sp) }
                )
                IconButton(
                    onClick = { viewModel.removeIngredient(ingredient) },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, stringResource(R.string.remove))
                }
            }
        }
    }
}

@Composable
fun NutritionItem(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = unit,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScanMealScreenPreview() {
    ScanMealScreen(navController = rememberNavController(), baseRecipeJson = null)
}