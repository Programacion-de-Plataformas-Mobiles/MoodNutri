package com.example.moodnutri.data

import com.example.moodnutri.data.models.openAi.GeneratedRecipe
import com.example.moodnutri.data.models.theMealDb.MealDetails
import com.example.moodnutri.data.network.TheMealDbApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecipeRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val theMealDbApi: TheMealDbApiService
) {

    private val currentUser by lazy { firebaseAuth.currentUser }

    suspend fun findRecipesByIngredients(ingredients: List<String>): List<MealDetails> = withContext(Dispatchers.IO) {
        // First, gather all unique recipe summaries from all ingredients
        val uniqueSummaries = ingredients
            .mapNotNull { ingredient ->
                try {
                    theMealDbApi.searchByIngredient(ingredient).meals
                } catch (e: Exception) {
                    null // Safely ignore failures for single ingredient searches
                }
            }
            .flatten()
            .distinctBy { it.idMeal }

        // Now, for each unique summary, fetch its full details
        uniqueSummaries.mapNotNull { summary ->
            try {
                theMealDbApi.getMealDetails(summary.idMeal).meals?.firstOrNull()
            } catch (e: Exception) {
                null // Safely ignore failures for single detail fetches
            }
        }
    }

    fun saveRecipe(recipe: GeneratedRecipe) {
        currentUser?.uid?.let { userId ->
            val databaseReference = firebaseDatabase.getReference("users").child(userId).child("recipes")
            databaseReference.child(recipe.name).setValue(recipe)
        }
    }

    fun saveFavorite(recipe: GeneratedRecipe) {
        currentUser?.uid?.let { userId ->
            val databaseReference = firebaseDatabase.getReference("users").child(userId).child("favorites")
            databaseReference.child(recipe.name).setValue(recipe)
        }
    }

    fun removeFavorite(recipeName: String) {
        currentUser?.uid?.let { userId ->
            val databaseReference = firebaseDatabase.getReference("users").child(userId).child("favorites")
            databaseReference.child(recipeName).removeValue()
        }
    }

    // MÉTODO NUEVO AÑADIDO: Guardar últimas 10 recetas en Firebase
    suspend fun saveRecentRecipe(recipe: GeneratedRecipe) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = firebaseDatabase.getReference("users/$userId/recent_recipes")

        // Obtener actuales
        val snapshot = ref.get().await()
        val current = snapshot.children.mapNotNull { it.getValue(GeneratedRecipe::class.java) }.toMutableList()

        // Añadir nueva
        current.add(0, recipe) // nueva primera

        // Mantener solo 10 y guardar
        ref.setValue(current.take(10)).await()
    }
}