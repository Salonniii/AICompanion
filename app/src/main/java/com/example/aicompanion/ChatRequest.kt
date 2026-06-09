package com.example.aicompanion

data class ChatRequest(
    val model: String,
    val messages: List<MessageData>
)

data class MessageData(
    val role: String,
    val content: String
)