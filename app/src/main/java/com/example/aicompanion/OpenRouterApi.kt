package com.example.aicompanion

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {

    @POST("chat/completions")
    suspend fun getChatResponse(
        @Header("Authorization") auth: String,
        @Body request: ChatRequest
    ): ChatResponse
}