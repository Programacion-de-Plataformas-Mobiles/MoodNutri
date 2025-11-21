// File: data/remote/api/TheMealDbApiService.kt
package com.example.moodnutri.data.remote.api

import com.example.moodnutri.data.models.themealdb.MealDetailsResponse
import com.example.moodnutri.data.models.themealdb.MealSummariesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMealDbApiService {

    @GET("filter.php")
    suspend fun searchByIngredient(@Query("i") ingredient: String): MealSummariesResponse

    @GET("lookup.php")
    suspend fun getMealDetails(@Query("i") id: String): MealDetailsResponse
}