package com.example.moodnutri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.moodnutri.mockups.LoginScreen
import com.example.moodnutri.mockups.SharedBottomNavigationBar
import com.example.moodnutri.ui.theme.MoodNutriTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoodNutriTheme {
                LoginScreen()
            }
        }
    }
}