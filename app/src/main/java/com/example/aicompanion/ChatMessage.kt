package com.example.aicompanion

data class ChatMessage(
    val id: String = "",
    val message: String = "",
    val imageUrl: String = "",
    val isUser: Boolean = false,
    val fileName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)