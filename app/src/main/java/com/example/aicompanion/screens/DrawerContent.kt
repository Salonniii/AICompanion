package com.example.aicompanion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DrawerContent(
    onHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "🤖 AICompanion",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onHistoryClick,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Chat History")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Logout")
        }
    }
}