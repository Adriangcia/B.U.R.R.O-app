package com.app.burro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.burro.model.GameState
import com.app.burro.model.Player
import com.app.burro.model.TurnResult
import com.app.burro.viewmodel.GameUiState

private const val PALABRA_BURRO = "BURRO"

@Composable
fun GameScreen(
    uiState: GameUiState,
    onSolicitarTruco: () -> Unit,
    onResultado: (TurnResult) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Marcador de todos los jugadores
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(uiState.jugadores) { jugador ->
                PlayerBadge(
                    jugador = jugador,
                    esActual = jugador.id == uiState.jugadorActual?.id
                )
            }
        }

        if (uiState.estado == GameState.SUDDEN_DEATH) {
            Text(
                text = "¡MUERTE SÚBITA!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Turno actual
        Text(
            text = "Turno de:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = uiState.jugadorActual?.nombre ?: "",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Truco actual, o botón para pedirlo
        if (uiState.truckActualDelTurno == null) {
            Button(
                onClick = onSolicitarTruco,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(160.dp)
            ) {
                Text(text = "TRUCO", style = MaterialTheme.typography.titleLarge)
            }
        } else {
            Text(
                text = if (uiState.esIniciador) "Tu truco:" else "Repite el truco:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                text = uiState.truckActualDelTurno.nombre,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onResultado(TurnResult.FAIL) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("FAIL")
                }

                if (uiState.esIniciador) {
                    Button(
                        onClick = { onResultado(TurnResult.PASAR) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("PASAR")
                    }
                }

                Button(
                    onClick = { onResultado(TurnResult.CHECK) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("CHECK")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PlayerBadge(jugador: Player, esActual: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = jugador.nombre,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (esActual) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = letrasVisibles(jugador.letrasAcumuladas),
            style = MaterialTheme.typography.titleMedium,
            color = if (jugador.eliminado) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

private fun letrasVisibles(letrasAcumuladas: Int): String {
    return PALABRA_BURRO.mapIndexed { index, letra ->
        if (index < letrasAcumuladas) letra else '·'
    }.joinToString("")
}