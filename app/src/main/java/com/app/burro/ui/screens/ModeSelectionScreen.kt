package com.app.burro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.burro.model.GameMode

@Composable
fun ModeSelectionScreen(
    onModoElegido: (GameMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Elige el modo de juego",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        ModoButton(
            titulo = "STANDARD",
            descripcion = "Basic → Basic+Medium → Todo",
            onClick = { onModoElegido(GameMode.STANDARD) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ModoButton(
            titulo = "BASIC",
            descripcion = "Solo trucos básicos",
            onClick = { onModoElegido(GameMode.BASIC) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ModoButton(
            titulo = "MEDIUM",
            descripcion = "Solo trucos medios",
            onClick = { onModoElegido(GameMode.MEDIUM) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ModoButton(
            titulo = "PRO",
            descripcion = "Solo trucos profesionales",
            onClick = { onModoElegido(GameMode.PRO) }
        )
    }
}

@Composable
private fun ModoButton(
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium)
            Text(text = descripcion, style = MaterialTheme.typography.bodySmall)
        }
    }
}