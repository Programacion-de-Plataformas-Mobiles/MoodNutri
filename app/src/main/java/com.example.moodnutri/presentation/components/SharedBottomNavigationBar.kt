// File: presentation/components/SharedBottomNavigationBar.kt
package com.example.moodnutri.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.moodnutri.R

@Composable
fun SharedBottomNavigationBar(
    navController: NavController,
    selectedTab: String
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
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
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
            label = { Text(stringResource(R.string.nav_home), fontSize = 10.sp) },
            selected = selectedTab == "Home",
            onClick = { navigateToScreen("home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Fastfood, contentDescription = stringResource(R.string.nav_ingredients)) },
            label = { Text(stringResource(R.string.nav_ingredients), fontSize = 10.sp) },
            selected = selectedTab == "Ingredients",
            onClick = { navigateToScreen("scan_ingredients") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Restaurant, contentDescription = stringResource(R.string.nav_recipes)) },
            label = { Text(stringResource(R.string.nav_recipes), fontSize = 10.sp) },
            selected = selectedTab == "Recipes",
            onClick = {
                val popped = navController.popBackStack("recipes_destination", inclusive = false)
                if (!popped) {
                    navigateToScreen("recipes_destination")
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = stringResource(R.string.nav_favorites)) },
            label = { Text(stringResource(R.string.nav_favorites), fontSize = 10.sp) },
            selected = selectedTab == "Favorites",
            onClick = { navigateToScreen("favorites") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.nav_profile)) },
            label = { Text(stringResource(R.string.nav_profile), fontSize = 10.sp) },
            selected = selectedTab == "Profile",
            onClick = { navigateToScreen("profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}