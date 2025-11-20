package com.example.moodnutri.data.repository

import android.util.Log
import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseRecipeRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    private fun getUserRecipesCollection() = getCurrentUserId()?.let { userId ->
        firestore.collection("users").document(userId).collection("recipes")
    }

    /**
     * Guarda una receta como "saved" (no favorita)
     * Mantiene máximo 10 recetas saved
     */
    suspend fun saveRecipe(recipe: FirebaseRecipe): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            Log.d("FirebaseRepo", "=== SAVING RECIPE ===")
            Log.d("FirebaseRepo", "User ID: $userId")
            Log.d("FirebaseRepo", "Recipe ID: ${recipe.id}")
            Log.d("FirebaseRepo", "Recipe Name: ${recipe.name}")
            Log.d("FirebaseRepo", "isFavorite: ${recipe.isFavorite}")

            if (userId == null) {
                Log.e("FirebaseRepo", " User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            val collection = getUserRecipesCollection()
                ?: return Result.failure(Exception("User not authenticated"))

            Log.d("FirebaseRepo", " Firebase path: users/$userId/recipes/${recipe.id}")

            // Primero verificar si ya existe
            val existingDoc = collection.document(recipe.id).get().await()
            Log.d("FirebaseRepo", "Existing document found: ${existingDoc.exists()}")

            if (existingDoc.exists()) {
                // Si ya existe, solo actualizar timestamp
                Log.d("FirebaseRepo", "Document exists, updating timestamp...")
                collection.document(recipe.id).update(
                    mapOf(
                        "timestamp" to System.currentTimeMillis(),
                        "isFavorite" to recipe.isFavorite
                    )
                ).await()
                Log.d("FirebaseRepo", " Recipe updated: ${recipe.name}")
            } else {
                // Es una receta nueva - obtener todas las recetas saved
                Log.d("FirebaseRepo", "Document doesn't exist, checking saved count...")
                val allSavedSnapshot = collection
                    .whereEqualTo("isFavorite", false)
                    .get()
                    .await()

                val savedRecipes = allSavedSnapshot.documents
                    .mapNotNull { it.toObject(FirebaseRecipe::class.java) }
                    .sortedBy { it.timestamp }

                Log.d("FirebaseRepo", "Current saved recipes count: ${savedRecipes.size}")

                // Si ya hay 10, eliminar la más antigua
                if (savedRecipes.size >= 10) {
                    val oldestRecipe = savedRecipes.first()
                    Log.d("FirebaseRepo", "Limit reached, deleting oldest: ${oldestRecipe.name}")
                    collection.document(oldestRecipe.id).delete().await()
                    Log.d("FirebaseRepo", " Deleted oldest recipe")
                }

                // Guardar la nueva receta
                val recipeWithUser = recipe.copy(
                    userId = userId,
                    isFavorite = false,
                    timestamp = System.currentTimeMillis()
                )

                Log.d("FirebaseRepo", "💾 About to save recipe to Firestore...")
                Log.d("FirebaseRepo", "Recipe data: name=${recipeWithUser.name}, userId=${recipeWithUser.userId}, isFavorite=${recipeWithUser.isFavorite}")

                collection.document(recipe.id).set(recipeWithUser).await()
                Log.d("FirebaseRepo", " Recipe saved successfully: ${recipe.name}")

                // Verificar que se guardó correctamente
                val verifyDoc = collection.document(recipe.id).get().await()
                if (verifyDoc.exists()) {
                    Log.d("FirebaseRepo", " VERIFICATION: Recipe exists in Firebase")
                    val verifyRecipe = verifyDoc.toObject(FirebaseRecipe::class.java)
                    Log.d("FirebaseRepo", " VERIFICATION: Recipe name: ${verifyRecipe?.name}")
                } else {
                    Log.e("FirebaseRepo", " VERIFICATION FAILED: Recipe NOT found in Firebase")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", " Error saving recipe: ${e.message}", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Agrega/remueve una receta de favoritos
     */
    suspend fun toggleFavorite(recipe: FirebaseRecipe, isFavorite: Boolean): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            Log.d("FirebaseRepo", "=== TOGGLING FAVORITE ===")
            Log.d("FirebaseRepo", "User ID: $userId")
            Log.d("FirebaseRepo", "Recipe ID: ${recipe.id}")
            Log.d("FirebaseRepo", "isFavorite: $isFavorite")

            if (userId == null) {
                Log.e("FirebaseRepo", "User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            val collection = getUserRecipesCollection()
                ?: return Result.failure(Exception("User not authenticated"))

            val recipeWithUser = recipe.copy(
                userId = userId,
                isFavorite = isFavorite,
                timestamp = System.currentTimeMillis()
            )

            if (isFavorite) {
                // Agregar a favoritos
                collection.document(recipe.id).set(recipeWithUser).await()
                Log.d("FirebaseRepo", " Added to favorites: ${recipe.name}")
            } else {
                // Verificar si existe como saved
                val doc = collection.document(recipe.id).get().await()
                if (doc.exists() && doc.getBoolean("isFavorite") == false) {
                    // Ya existe como saved, solo actualizar el flag
                    collection.document(recipe.id).update("isFavorite", false).await()
                    Log.d("FirebaseRepo", "Updated isFavorite flag to false")
                } else {
                    // No existe o era favorito puro, eliminar
                    collection.document(recipe.id).delete().await()
                    Log.d("FirebaseRepo", "Removed from favorites: ${recipe.name}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", " Error toggling favorite: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene todas las recetas guardadas (saved + favorites)
     */
    suspend fun getAllRecipes(): Result<List<FirebaseRecipe>> {
        return try {
            val userId = getCurrentUserId()
            Log.d("FirebaseRepo", "=== GETTING ALL RECIPES ===")
            Log.d("FirebaseRepo", "User ID: $userId")

            if (userId == null) {
                Log.e("FirebaseRepo", " User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            val collection = getUserRecipesCollection()
                ?: return Result.failure(Exception("User not authenticated"))

            Log.d("FirebaseRepo", " Firebase path: users/$userId/recipes")

            val snapshot = collection.get().await()

            Log.d("FirebaseRepo", " Snapshot size: ${snapshot.documents.size}")

            val recipes = snapshot.documents
                .mapNotNull {
                    val recipe = it.toObject(FirebaseRecipe::class.java)
                    Log.d("FirebaseRepo", "  📄 Document ID: ${it.id}, Recipe: ${recipe?.name}, isFavorite: ${recipe?.isFavorite}")
                    recipe
                }
                .sortedByDescending { it.timestamp }

            Log.d("FirebaseRepo", " Retrieved ${recipes.size} total recipes")
            recipes.forEach {
                Log.d("FirebaseRepo", "  - ${it.name} (isFavorite: ${it.isFavorite}, timestamp: ${it.timestamp})")
            }

            Result.success(recipes)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", " Error getting all recipes: ${e.message}", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Obtiene solo las recetas saved (no favoritas)
     */
    suspend fun getSavedRecipes(): Result<List<FirebaseRecipe>> {
        return try {
            val userId = getCurrentUserId()
            Log.d("FirebaseRepo", "=== GETTING SAVED RECIPES ===")
            Log.d("FirebaseRepo", "User ID: $userId")

            if (userId == null) {
                Log.e("FirebaseRepo", " User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            val collection = getUserRecipesCollection()
                ?: return Result.failure(Exception("User not authenticated"))

            Log.d("FirebaseRepo", " Firebase path: users/$userId/recipes")
            Log.d("FirebaseRepo", " Filtering by: isFavorite = false")

            // Query simple sin orderBy
            val snapshot = collection
                .whereEqualTo("isFavorite", false)
                .get()
                .await()

            Log.d("FirebaseRepo", "Snapshot size: ${snapshot.documents.size}")

            // Ordenar manualmente en el código
            val recipes = snapshot.documents
                .mapNotNull { doc ->
                    val recipe = doc.toObject(FirebaseRecipe::class.java)
                    Log.d("FirebaseRepo", "   Document ID: ${doc.id}")
                    Log.d("FirebaseRepo", "     Name: ${recipe?.name}")
                    Log.d("FirebaseRepo", "     isFavorite: ${recipe?.isFavorite}")
                    Log.d("FirebaseRepo", "     timestamp: ${recipe?.timestamp}")
                    Log.d("FirebaseRepo", "     userId: ${recipe?.userId}")
                    recipe
                }
                .sortedByDescending { it.timestamp }
                .take(10)

            Log.d("FirebaseRepo", "Retrieved ${recipes.size} saved recipes")
            recipes.forEachIndexed { index, recipe ->
                Log.d("FirebaseRepo", "  ${index + 1}. ${recipe.name} (isFavorite: ${recipe.isFavorite})")
            }

            if (recipes.isEmpty()) {
                Log.w("FirebaseRepo", "⚠ NO SAVED RECIPES FOUND!")
                Log.w("FirebaseRepo", "Please check:")
                Log.w("FirebaseRepo", "1. Are you logged in? User ID: $userId")
                Log.w("FirebaseRepo", "2. Have you saved any recipes?")
                Log.w("FirebaseRepo", "3. Check Firebase Console: users/$userId/recipes")
            }

            Result.success(recipes)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", " Error getting saved recipes: ${e.message}", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Obtiene solo las recetas favoritas
     */
    suspend fun getFavoriteRecipes(): Result<List<FirebaseRecipe>> {
        return try {
            val userId = getCurrentUserId()
            Log.d("FirebaseRepo", "=== GETTING FAVORITE RECIPES ===")
            Log.d("FirebaseRepo", "User ID: $userId")

            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val collection = getUserRecipesCollection()
                ?: return Result.failure(Exception("User not authenticated"))

            // Query simple sin orderBy
            val snapshot = collection
                .whereEqualTo("isFavorite", true)
                .get()
                .await()

            // Ordenar manualmente en el código
            val recipes = snapshot.documents
                .mapNotNull { it.toObject(FirebaseRecipe::class.java) }
                .sortedByDescending { it.timestamp }

            Log.d("FirebaseRepo", "Retrieved ${recipes.size} favorite recipes")

            Result.success(recipes)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", " Error getting favorite recipes: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Verifica si una receta es favorita
     */
    suspend fun isFavorite(recipeId: String): Boolean {
        return try {
            val collection = getUserRecipesCollection() ?: return false
            val doc = collection.document(recipeId).get().await()
            val result = doc.exists() && doc.getBoolean("isFavorite") == true
            Log.d("FirebaseRepo", "Is favorite check for $recipeId: $result")
            result
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error checking if favorite", e)
            false
        }
    }
}