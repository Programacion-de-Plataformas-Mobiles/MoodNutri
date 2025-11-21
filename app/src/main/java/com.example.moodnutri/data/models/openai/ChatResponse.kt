package com.example.moodnutri.data.models.openai

data class ChatResponse(val choices: List<Choice>)

data class Choice(val message: ChatMessage)
