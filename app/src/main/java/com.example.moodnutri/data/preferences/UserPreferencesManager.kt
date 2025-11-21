package com.example.moodnutri.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {

    companion object {
        private val DAILY_CALORIE_GOAL = intPreferencesKey("daily_calorie_goal")
        private val DAILY_PROTEIN_GOAL = intPreferencesKey("daily_protein_goal")
        private val DAILY_CARBS_GOAL = intPreferencesKey("daily_carbs_goal")
        private val LANGUAGE = stringPreferencesKey("language")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val PROFILE_PHOTO_URI = stringPreferencesKey("profile_photo_uri")
        private val CURRENT_MOOD = stringPreferencesKey("current_mood")
        private val CURRENT_EMOJI = stringPreferencesKey("current_emoji")
    }

    val dailyCalorieGoal: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_CALORIE_GOAL] ?: 2000
    }

    val dailyProteinGoal: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_PROTEIN_GOAL] ?: 150
    }

    val dailyCarbsGoal: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_CARBS_GOAL] ?: 250
    }

    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LANGUAGE] ?: "en"
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: "system"
    }

    val profilePhotoUri: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PROFILE_PHOTO_URI] ?: ""
    }

    val currentMood: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CURRENT_MOOD] ?: "Happy"
    }

    val currentEmoji: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CURRENT_EMOJI] ?: "😊"
    }

    suspend fun setDailyCalorieGoal(calories: Int) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_CALORIE_GOAL] = calories
        }
    }

    suspend fun setDailyProteinGoal(protein: Int) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_PROTEIN_GOAL] = protein
        }
    }

    suspend fun setDailyCarbsGoal(carbs: Int) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_CARBS_GOAL] = carbs
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE] = lang
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    suspend fun setProfilePhotoUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[PROFILE_PHOTO_URI] = uri
        }
    }

    suspend fun setCurrentMood(mood: String, emoji: String) {
        context.dataStore.edit { prefs ->
            prefs[CURRENT_MOOD] = mood
            prefs[CURRENT_EMOJI] = emoji
        }
    }
}