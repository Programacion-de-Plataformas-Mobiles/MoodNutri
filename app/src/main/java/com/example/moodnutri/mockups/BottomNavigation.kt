package com.example.moodnutri.mockups

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination


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
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        NavigationBarItem(
            // FIX: Added 'imageVector ='
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
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
            // FIX: Added 'imageVector ='
            icon = { Icon(imageVector = Icons.Default.Fastfood, contentDescription = "Ingredients") },
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
        NavigationBarItem(
            // FIX: Added 'imageVector ='
            icon = { Icon(imageVector = Icons.Default.Restaurant, contentDescription = "Recipes") },
            label = { Text("Recipes", fontSize = 10.sp) },
            selected = selectedTab == "Recipes",
            onClick = { navigateToScreen("recipes_destination") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            // FIX: Added 'imageVector ='
            icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorites") },
            label = { Text("Favorites", fontSize = 10.sp) },
            selected = selectedTab == "Favorites",
            onClick = { navigateToScreen("favorites") },
            onClick = { 
                // Lógica especial para Recipes: Si ya estamos en el stack de navegación de Recipes 
                // (por ejemplo, en ScanMeal pulsado desde Recipes), intentamos volver a Recipes.
                // Si no está en el stack, navegamos normalmente.
                val popped = navController.popBackStack("recipes_destination", inclusive = false)
                if (!popped) {
                    navigateToScreen("recipes_destination")
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            // FIX: Added 'imageVector ='
            icon = { Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Scan Meal") },
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
            // FIX: Added 'imageVector ='
            icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
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
