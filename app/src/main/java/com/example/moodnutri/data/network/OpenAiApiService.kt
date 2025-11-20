package com.example.moodnutri.data.network

import com.example.moodnutri.BuildConfig
import com.example.moodnutri.data.models.openAi.ChatRequest
import com.example.moodnutri.data.models.openAi.ChatResponse
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
