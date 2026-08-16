package com.app.burro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

@Composable
fun PlayerSetupScreen(
    onJugadoresListos: (List<String>) -> Unit
) {
    var numeroJugadores by remember { mutableStateOf(2) }
    var nombres by remember { mutableStateOf(List(2) { "" }) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "BURRO",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Número de jugadores: $numeroJugadores")

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Button(
                onClick = {
                    if (numeroJugadores > 2) {
                        numeroJugadores--
                        nombres = nombres.dropLast(1)
                    }
                }
            ) { Text("-") }

            Button(
                onClick = {
                    if (numeroJugadores < 8) {
                        numeroJugadores++
                        nombres = nombres + ""
                    }
                }
            ) { Text("+") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(numeroJugadores) { index ->
                OutlinedTextField(
                    value = nombres[index],
                    onValueChange = { nuevoNombre ->
                        nombres = nombres.toMutableList().also { it[index] = nuevoNombre }
                    },
                    label = { Text("Jugador ${index + 1}") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val todosRellenos = nombres.all { it.isNotBlank() }

        Button(
            onClick = { onJugadoresListos(nombres) },
            enabled = todosRellenos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar")
        }
    }
}