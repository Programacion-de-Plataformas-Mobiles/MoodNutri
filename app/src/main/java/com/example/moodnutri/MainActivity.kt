package com.example.moodnutri

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.ui.theme.MoodNutriTheme
import com.example.moodnutri.utils.LocaleHelper
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val preferencesManager = UserPreferencesManager(newBase)
        val language = runBlocking {
            preferencesManager.language.firstOrNull() ?: "en"
        }
        val context = LocaleHelper.setLocale(newBase, language)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            val preferencesManager = UserPreferencesManager(applicationContext)
            val themeMode by preferencesManager.themeMode.collectAsState(initial = "system")

            MoodNutriTheme(themeMode = themeMode) {
                AppNavigation()
            }
        }
    }
}