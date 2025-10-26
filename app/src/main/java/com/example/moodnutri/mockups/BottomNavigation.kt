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
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 10.sp) },
            selected = selectedTab == "Home",
            onClick = { navController.navigate("home") },
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
            onClick = { navController.navigate("scan_ingredients") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Restaurant, contentDescription = "Recipes") },
            label = { Text("Recipes", fontSize = 10.sp) },
            selected = selectedTab == "Recipes",
            onClick = { navController.navigate("recipe") },
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
            onClick = { navController.navigate("scan_meal") },
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
            onClick = { navController.navigate("profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                selectedTextColor = Color(0xFF4CAF50),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}