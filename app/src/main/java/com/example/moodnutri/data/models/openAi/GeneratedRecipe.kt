package com.example.moodnutri.data.models.openAi

// Este modelo representa la estructura JSON estricta que le pedimos a ChatGPT.
data class GeneratedRecipe(
    val name: String,
    val time: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val image_url: String,
    val reason: String // Nuevo campo para la explicación
)
