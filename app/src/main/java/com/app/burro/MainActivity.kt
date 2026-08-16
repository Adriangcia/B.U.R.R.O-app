package com.app.burro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.burro.model.AppScreen
import com.app.burro.model.GameMode
import com.app.burro.ui.screens.ModeSelectionScreen
import com.app.burro.ui.screens.PlayerSetupScreen
import com.app.burro.ui.theme.BURROTheme
import com.app.burro.viewmodel.GameViewModel
import com.app.burro.viewmodel.GameViewModelFactory
import androidx.compose.runtime.collectAsState
import com.app.burro.ui.screens.GameScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BURROTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BurroApp()
                }
            }
        }
    }
}

@Composable
fun BurroApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
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

            if (state.estado == com.app.burro.model.GameState.FINISHED) {
                pantallaActual = AppScreen.WINNER
            } else {
                GameScreen(
                    uiState = state,
                    onSolicitarTruco = { viewModel.solicitarTruco() },
                    onResultado = { resultado -> viewModel.registrarResultado(resultado) }
                )
            }
        }

        AppScreen.WINNER -> {
            // TODO: pantalla de victoria
        }
    }
}