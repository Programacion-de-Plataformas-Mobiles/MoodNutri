// File: data/remote/api/OpenAiApiService.kt
package com.example.moodnutri.data.remote.api

import com.example.moodnutri.BuildConfig
import com.example.moodnutri.data.models.openai.ChatRequest
import com.example.moodnutri.data.models.openai.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApiService {

    @POST("chat/completions")
    suspend fun getRecipeSuggestion(
        @Header("Authorization") apiKey: String = "Bearer ${BuildConfig.CHATGPT_API_KEY}",
        @Body request: ChatRequest
    ): ChatResponse
}