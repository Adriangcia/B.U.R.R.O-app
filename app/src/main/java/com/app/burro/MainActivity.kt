package com.app.burro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.burro.model.AppScreen
import com.app.burro.model.GameState
import com.app.burro.ui.screens.GameScreen
import com.app.burro.ui.screens.ModeSelectionScreen
import com.app.burro.ui.screens.PlayerSetupScreen
import com.app.burro.ui.screens.WinnerScreen
import com.app.burro.ui.theme.BURROTheme
import com.app.burro.viewmodel.GameViewModel
import com.app.burro.viewmodel.GameViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BURROTheme {
                BurroApp()
            }
        }
    }
}

@Composable
fun BurroApp() {
    val context = LocalContext.current
    val viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(context))

    var pantallaActual by remember { mutableStateOf(AppScreen.PLAYER_SETUP) }
    var nombresGuardados by remember { mutableStateOf<List<String>>(emptyList()) }

    when (pantallaActual) {
        AppScreen.PLAYER_SETUP -> {
            PlayerSetupScreen(
                onJugadoresListos = { nombres ->
                    nombresGuardados = nombres
                    pantallaActual = AppScreen.MODE_SELECTION
                }
            )
        }

        AppScreen.MODE_SELECTION -> {
            ModeSelectionScreen(
                onModoElegido = { modo ->
                    viewModel.startGame(nombresGuardados, modo)
                    pantallaActual = AppScreen.GAME
                }
            )
        }

        AppScreen.GAME -> {
            val state by viewModel.uiState.collectAsState()

            if (state.estado == GameState.FINISHED) {
                pantallaActual = AppScreen.WINNER
            } else {
                GameScreen(
                    uiState = state,
                    onSolicitarTruco = { viewModel.solicitarTruco() },
                    onResultado = { resultado -> viewModel.registrarResultado(resultado) },
                    onDescartarAvisoEliminacion = { viewModel.descartarAvisoEliminacion() }
                )
            }
        }

        AppScreen.WINNER -> {
            val state by viewModel.uiState.collectAsState()

            WinnerScreen(
                ganador = state.ganador,
                onNuevaPartida = {
                    pantallaActual = AppScreen.PLAYER_SETUP
                }
            )
        }
    }
}