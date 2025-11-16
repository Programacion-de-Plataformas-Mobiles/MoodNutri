package com.example.moodnutri.mockups

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SharedBottomNavigationBar(
    navController: NavController,
    selectedTab: String
) {
    NavigationBar(
        containerColor = Color.White,
    ) {
        val navigateToScreen = { route: String ->
            navController.navigate(route) {
                popUpTo("home") { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 10.sp) },
            selected = selectedTab == "Home",
            onClick = { navigateToScreen("home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Fastfood, contentDescription = "Ingredients") },
            label = { Text("Ingredients", fontSize = 10.sp) },
            selected = selectedTab == "Ingredients",
            onClick = { navigateToScreen("scan_ingredients") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        // Restauramos la pestaña "Recipes"
        NavigationBarItem(
            icon = { Icon(Icons.Default.Restaurant, contentDescription = "Recipes") },
            label = { Text("Recipes", fontSize = 10.sp) },
            selected = selectedTab == "Recipes",
            onClick = { navigateToScreen("recipes_destination") }, // Nueva ruta estable
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.PhotoCamera, contentDescription = "Scan Meal") },
            label = { Text("Scan Meal", fontSize = 10.sp) },
            selected = selectedTab == "Scan Meal",
            onClick = { navigateToScreen("scan_meal") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 10.sp) },
            selected = selectedTab == "Profile",
            onClick = { navigateToScreen("profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}
