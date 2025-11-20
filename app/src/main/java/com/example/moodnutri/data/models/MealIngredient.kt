package com.example.moodnutri.data.models

import java.util.UUID

data class MealIngredient(
    val name: String,
    val quantity: String,
    val id: String = UUID.randomUUID().toString() // Identificador único y estable
)
