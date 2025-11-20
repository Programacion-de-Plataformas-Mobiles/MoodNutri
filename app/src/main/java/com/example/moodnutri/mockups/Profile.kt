package com.example.moodnutri.mockups

import android.net.Uri
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.moodnutri.R
import com.example.moodnutri.viewmodel.AuthViewModel
import com.example.moodnutri.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val dailyCalorieGoal by profileViewModel.dailyCalorieGoal.collectAsState()
    val dailyProteinGoal by profileViewModel.dailyProteinGoal.collectAsState()
    val dailyCarbsGoal by profileViewModel.dailyCarbsGoal.collectAsState()
    val todayNutrition by profileViewModel.todayNutrition.collectAsState()
    val themeMode by profileViewModel.themeMode.collectAsState()
    val language by profileViewModel.language.collectAsState()
    val profilePhotoUri by profileViewModel.profilePhotoUri.collectAsState()
    val currentMood by profileViewModel.currentMood.collectAsState()
    val currentEmoji by profileViewModel.currentEmoji.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCalorieDialog by remember { mutableStateOf(false) }
    var showProteinDialog by remember { mutableStateOf(false) }
    var showCarbsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Launcher para seleccionar foto de galería
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { profileViewModel.setProfilePhoto(it) }
    }

    Scaffold(
        topBar = { SharedTopAppBar() },
        bottomBar = { SharedBottomNavigationBar(navController = navController, selectedTab = "Profile") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // Foto de perfil y nombre
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box {
                    if (profilePhotoUri != null && profilePhotoUri!!.isNotEmpty()) {
                        AsyncImage(
                            model = profilePhotoUri,
                            contentDescription = stringResource(R.string.change_photo),
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.change_photo),
                            modifier = Modifier
                                .size(100.dp)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Botón para cambiar foto
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.change_photo),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentUser?.email?.substringBefore("@") ?: "User",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currentUser?.email ?: "",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Estado emocional
            Text(
                text = stringResource(R.string.emotional_state),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentEmoji,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = currentMood,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nutrición diaria
            Text(
                text = stringResource(R.string.nutrition),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Calorías
            NutritionCard(
                icon = Icons.Default.LocalFireDepartment,
                label = stringResource(R.string.calories),
                current = todayNutrition.caloriesConsumed,
                goal = dailyCalorieGoal,
                unit = stringResource(R.string.kcal),
                color = MaterialTheme.colorScheme.primary,
                onClick = { showCalorieDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Proteína
            NutritionCard(
                icon = Icons.Default.FitnessCenter,
                label = stringResource(R.string.protein),
                current = todayNutrition.proteinConsumed,
                goal = dailyProteinGoal,
                unit = stringResource(R.string.grams),
                color = Color(0xFFFF6B6B),
                onClick = { showProteinDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Carbohidratos
            NutritionCard(
                icon = Icons.Default.Grain,
                label = stringResource(R.string.carbs),
                current = todayNutrition.carbsConsumed,
                goal = dailyCarbsGoal,
                unit = stringResource(R.string.grams),
                color = Color(0xFF4ECDC4),
                onClick = { showCarbsDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Configuración
            Text(
                text = stringResource(R.string.settings),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Idioma
            SettingItem(
                icon = Icons.Default.Language,
                title = stringResource(R.string.language),
                subtitle = when(language) {
                    "es" -> stringResource(R.string.language_spanish)
                    "fr" -> stringResource(R.string.language_french)
                    else -> stringResource(R.string.language_english)
                },
                onClick = { showLanguageDialog = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tema
            SettingItem(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.theme),
                subtitle = when(themeMode) {
                    "light" -> stringResource(R.string.theme_light)
                    "dark" -> stringResource(R.string.theme_dark)
                    else -> stringResource(R.string.theme_system)
                },
                onClick = { showThemeDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de Logout
            Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = stringResource(R.string.logout),
                    tint = MaterialTheme.colorScheme.onError
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.logout),
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Diálogos
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = language,
            onLanguageSelected = { selectedLang ->
                profileViewModel.setLanguage(selectedLang)
                showLanguageDialog = false

                // Recrear la Activity para aplicar el nuevo idioma
                (context as? Activity)?.recreate()
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onThemeSelected = { selectedTheme ->
                profileViewModel.setThemeMode(selectedTheme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showCalorieDialog) {
        GoalInputDialog(
            title = stringResource(R.string.set_calorie_goal),
            currentValue = dailyCalorieGoal,
            onValueSet = { newGoal ->
                profileViewModel.setDailyCalorieGoal(newGoal)
                showCalorieDialog = false
            },
            onDismiss = { showCalorieDialog = false }
        )
    }

    if (showProteinDialog) {
        GoalInputDialog(
            title = stringResource(R.string.set_protein_goal),
            currentValue = dailyProteinGoal,
            onValueSet = { newGoal ->
                profileViewModel.setDailyProteinGoal(newGoal)
                showProteinDialog = false
            },
            onDismiss = { showProteinDialog = false }
        )
    }

    if (showCarbsDialog) {
        GoalInputDialog(
            title = stringResource(R.string.set_carbs_goal),
            currentValue = dailyCarbsGoal,
            onValueSet = { newGoal ->
                profileViewModel.setDailyCarbsGoal(newGoal)
                showCarbsDialog = false
            },
            onDismiss = { showCarbsDialog = false }
        )
    }
}

@Composable
fun NutritionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    current: Int,
    goal: Int,
    unit: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$current / $goal $unit",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            CircularProgressIndicator(
                progress = { (current.toFloat() / goal.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.size(60.dp),
                strokeWidth = 6.dp,
                color = color,
                trackColor = color.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                LanguageOption(
                    language = "en",
                    label = stringResource(R.string.language_english),
                    isSelected = currentLanguage == "en",
                    onClick = { onLanguageSelected("en") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LanguageOption(
                    language = "es",
                    label = stringResource(R.string.language_spanish),
                    isSelected = currentLanguage == "es",
                    onClick = { onLanguageSelected("es") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LanguageOption(
                    language = "fr",
                    label = stringResource(R.string.language_french),
                    isSelected = currentLanguage == "fr",
                    onClick = { onLanguageSelected("fr") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun LanguageOption(
    language: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.theme),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                ThemeOption(
                    theme = "light",
                    label = stringResource(R.string.theme_light),
                    icon = Icons.Default.LightMode,
                    isSelected = currentTheme == "light",
                    onClick = { onThemeSelected("light") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ThemeOption(
                    theme = "dark",
                    label = stringResource(R.string.theme_dark),
                    icon = Icons.Default.DarkMode,
                    isSelected = currentTheme == "dark",
                    onClick = { onThemeSelected("dark") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ThemeOption(
                    theme = "system",
                    label = stringResource(R.string.theme_system),
                    icon = Icons.Default.SettingsBrightness,
                    isSelected = currentTheme == "system",
                    onClick = { onThemeSelected("system") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ThemeOption(
    theme: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun GoalInputDialog(
    title: String,
    currentValue: Int,
    onValueSet: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var inputValue by remember { mutableStateOf(currentValue.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = {
                        inputValue = it
                        isError = it.toIntOrNull() == null || it.toIntOrNull()!! <= 0
                    },
                    label = { Text(stringResource(R.string.goal_hint)) },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = "Please enter a valid number greater than 0",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = inputValue.toIntOrNull()
                    if (value != null && value > 0) {
                        onValueSet(value)
                    }
                },
                enabled = !isError && inputValue.isNotEmpty()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}