package com.example.moodnutri.data.models.openai

data class GeneratedRecipe(
    val name: String,
    val time: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val image_url: String,
    val reason: String
)
