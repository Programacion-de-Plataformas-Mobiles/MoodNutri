// File: data/remote/RetrofitInstance.kt
package com.example.moodnutri.data.remote

import com.example.moodnutri.data.remote.api.OpenAiApiService
import com.example.moodnutri.data.remote.api.TheMealDbApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val mealDbApi: TheMealDbApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TheMealDbApiService::class.java)
    }

    val openAiApi: OpenAiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAiApiService::class.java)
    }
}