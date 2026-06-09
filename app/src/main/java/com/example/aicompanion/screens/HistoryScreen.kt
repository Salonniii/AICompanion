package com.example.aicompanion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HistoryScreen() {

    val chats = listOf(
        "How to learn Kotlin",
        "AI roadmap",
        "Spring Boot help"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        items(chats) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {

                Text(
                    text = it,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}