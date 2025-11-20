package com.example.moodnutri.data

import com.example.moodnutri.data.models.theMealDb.MealDetails
import com.example.moodnutri.data.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

// El repositorio es una capa de abstracción que se encarga de la lógica de obtención de datos.
class RecipeRepository {

    private val mealDbService = RetrofitInstance.mealDbApi

    /**
     * Busca recetas en TheMealDB que contengan CUALQUIERA de los ingredientes proporcionados.
     * @param ingredients Lista de ingredientes que el usuario tiene.
     * @return Una lista de recetas completas (MealDetails), sin duplicados.
     */
    suspend fun findRecipesByIngredients(ingredients: List<String>): List<MealDetails> = withContext(Dispatchers.IO) {
        if (ingredients.isEmpty()) return@withContext emptyList()

        // 1. Para cada ingrediente, busca las recetas que lo contienen.
        //    Esto se hace en paralelo para mayor eficiencia.
        val mealSummaries = ingredients.map {
            async { mealDbService.searchByIngredient(it).meals ?: emptyList() }
        }.awaitAll().flatten()

        // 2. Obtenemos una lista de IDs de recetas, eliminando duplicados.
        val uniqueMealIds = mealSummaries.map { it.idMeal }.distinct()

        // 3. Para cada ID único, obtenemos los detalles completos de la receta.
        //    Esto también se hace en paralelo.
        val mealDetailsList = uniqueMealIds.map {
            async { mealDbService.getMealDetails(it).meals?.firstOrNull() }
        }.awaitAll().filterNotNull()

        return@withContext mealDetailsList
    }
}
