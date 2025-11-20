package com.example.moodnutri.data.repository

import android.util.Log
import com.example.moodnutri.data.models.DailyNutrition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class NutritionRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun getTodayNutrition(): Result<DailyNutrition> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Not authenticated"))
            val today = getTodayDate()

            val doc = firestore.collection("users")
                .document(userId)
                .collection("daily_nutrition")
                .document(today)
                .get()
                .await()

            if (doc.exists()) {
                val nutrition = doc.toObject(DailyNutrition::class.java)
                Result.success(nutrition ?: DailyNutrition(date = today, userId = userId))
            } else {
                Result.success(DailyNutrition(date = today, userId = userId))
            }
        } catch (e: Exception) {
            Log.e("NutritionRepo", "Error getting today nutrition", e)
            Result.failure(e)
        }
    }

    suspend fun updateTodayNutrition(nutrition: DailyNutrition): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Not authenticated"))
            val today = getTodayDate()

            firestore.collection("users")
                .document(userId)
                .collection("daily_nutrition")
                .document(today)
                .set(nutrition.copy(date = today, userId = userId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NutritionRepo", "Error updating nutrition", e)
            Result.failure(e)
        }
    }

    suspend fun addMealToToday(recipeId: String, calories: Int, protein: Int, carbs: Int): Result<Unit> {
        return try {
            val nutritionResult = getTodayNutrition()
            if (nutritionResult.isFailure) return Result.failure(nutritionResult.exceptionOrNull()!!)

            val currentNutrition = nutritionResult.getOrNull()!!
            val updatedNutrition = currentNutrition.copy(
                caloriesConsumed = currentNutrition.caloriesConsumed + calories,
                proteinConsumed = currentNutrition.proteinConsumed + protein,
                carbsConsumed = currentNutrition.carbsConsumed + carbs,
                meals = currentNutrition.meals + recipeId
            )

            updateTodayNutrition(updatedNutrition)
        } catch (e: Exception) {
            Log.e("NutritionRepo", "Error adding meal", e)
            Result.failure(e)
        }
    }
}