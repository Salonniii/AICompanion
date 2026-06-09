package com.example.aicompanion

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val api: OpenRouterApi by lazy {

        Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(OpenRouterApi::class.java)
    }
}