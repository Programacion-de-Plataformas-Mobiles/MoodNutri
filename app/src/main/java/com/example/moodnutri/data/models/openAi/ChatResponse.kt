package com.example.moodnutri.data.models.openAi

data class ChatResponse(val choices: List<Choice>)

data class Choice(val message: ChatMessage)
