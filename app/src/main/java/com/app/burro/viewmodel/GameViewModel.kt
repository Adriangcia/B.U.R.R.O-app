package com.app.burro.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.burro.data.TrickRepository
import com.app.burro.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(context: Context) : ViewModel() {

    private val repository = TrickRepository(context)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var poolBasic: MutableList<Trick> = mutableListOf()
    private var poolMedium: MutableList<Trick> = mutableListOf()
    private var poolPro: MutableList<Trick> = mutableListOf()

    // Recuerda quién abrió la ronda actual, para saber a quién pasar
    // el turno de "iniciador" cuando la ronda se cierre.
    private var iniciadorRondaActualId: Long? = null

    fun startGame(nombresJugadores: List<String>, modo: GameMode) {
        viewModelScope.launch {
            poolBasic = repository.getTricksByNivel(TrickLevel.BASIC).toMutableList()
            poolMedium = repository.getTricksByNivel(TrickLevel.MEDIUM).toMutableList()
            poolPro = repository.getTricksByNivel(TrickLevel.PRO).toMutableList()

            val jugadores = nombresJugadores
                .mapIndexed { index, nombre -> Player(id = index.toLong(), nombre = nombre) }
                .shuffled()

            iniciadorRondaActualId = jugadores.firstOrNull()?.id

            _uiState.update {
                GameUiState(
                    modo = modo,
                    jugadores = jugadores,
                    turnoActualIndex = 0
                )
            }
        }
    }

    private val sublevelWeights = mapOf(1 to 50, 2 to 30, 3 to 20)

    private fun nivelesDisponibles(modo: GameMode, contador: Int): List<TrickLevel> {
        return when (modo) {
            GameMode.BASIC -> listOf(TrickLevel.BASIC)
            GameMode.MEDIUM -> listOf(TrickLevel.MEDIUM)
            GameMode.PRO -> listOf(TrickLevel.PRO)
            GameMode.STANDARD -> when {
                contador < 10 -> listOf(TrickLevel.BASIC)
                contador < 25 -> listOf(TrickLevel.BASIC, TrickLevel.MEDIUM)
                else -> listOf(TrickLevel.BASIC, TrickLevel.MEDIUM, TrickLevel.PRO)
            }
        }
    }

    private fun elegirTrickPonderado(candidatos: List<Trick>): Trick? {
        if (candidatos.isEmpty()) return null
        val pesos = candidatos.map { it to (sublevelWeights[it.sublevel] ?: 10) }
        val pesoTotal = pesos.sumOf { it.second }
        val aleatorio = Random.nextInt(pesoTotal)
        var acumulado = 0
        for ((trick, peso) in pesos) {
            acumulado += peso
            if (aleatorio < acumulado) return trick
        }
        return candidatos.last()
    }

    private fun candidatosDisponibles(state: GameUiState): List<Trick> {
        val niveles = nivelesDisponibles(state.modo, state.contadorTrucosGlobal)
        return niveles.flatMap { nivel ->
            when (nivel) {
                TrickLevel.BASIC -> poolBasic
                TrickLevel.MEDIUM -> poolMedium
                TrickLevel.PRO -> poolPro
            }
        }
    }

    private fun verificarTrucosDisponibles() {
        val state = _uiState.value
        if (state.estado == GameState.FINISHED || state.estado == GameState.NO_TRICKS_AVAILABLE) return

        val candidatos = candidatosDisponibles(state)

        if (candidatos.isEmpty()) {
            val mensaje = if (state.modo == GameMode.STANDARD) {
                "Se han agotado todos los trucos disponibles."
            } else {
                "Ya no hay más trucos de nivel ${state.modo.name}."
            }
            _uiState.update {
                it.copy(estado = GameState.NO_TRICKS_AVAILABLE, mensajeFin = mensaje)
            }
        }
    }

    /**
     * Llamado cuando el jugador actual (iniciador) pulsa el botón TRUCO.
     */
    fun solicitarTruco() {
        val state = _uiState.value
        val candidatos = candidatosDisponibles(state)
        val trickElegido = elegirTrickPonderado(candidatos) ?: return

        _uiState.update {
            it.copy(
                truckActualDelTurno = trickElegido,
                contadorTrucosGlobal = it.contadorTrucosGlobal + 1,
                cadenaActiva = false,
                jugadoresQueYaIntentaronEsteTruco = emptySet()
            )
        }
    }

    private fun ganarLetra(jugador: Player): Player {
        val nuevasLetras = jugador.letrasAcumuladas + 1
        return jugador.copy(
            letrasAcumuladas = nuevasLetras,
            pasesEnLetraActual = 0,
            eliminado = nuevasLetras >= 5
        )
    }

    /**
     * Llamado cuando el jugador actual pulsa FAIL, PASAR o CHECK.
     */
    fun registrarResultado(resultado: TurnResult) {
        val state = _uiState.value
        val jugadorActual = state.jugadorActual ?: return
        val trickActual = state.truckActualDelTurno ?: return

        if (state.esIniciador) {
            registrarResultadoIniciador(jugadorActual, trickActual, resultado)
        } else {
            registrarResultadoCadena(jugadorActual, resultado)
        }
    }

    private fun registrarResultadoIniciador(
        jugadorActual: Player,
        trickActual: Trick,
        resultado: TurnResult
    ) {
        when (resultado) {
            TurnResult.PASAR -> {
                val nuevoPases = jugadorActual.pasesEnLetraActual + 1
                val jugadorActualizado =
                    if (nuevoPases >= 3) ganarLetra(jugadorActual)
                    else jugadorActual.copy(pasesEnLetraActual = nuevoPases)

                actualizarJugador(jugadorActualizado)
                cerrarRondaSinCadena()
            }

            TurnResult.FAIL -> {
                // Sin consecuencias, simplemente pasa el turno
                cerrarRondaSinCadena()
            }

            TurnResult.CHECK -> {
                // El truco se fija y se descarta del pool
                when (trickActual.nivel) {
                    TrickLevel.BASIC -> poolBasic.remove(trickActual)
                    TrickLevel.MEDIUM -> poolMedium.remove(trickActual)
                    TrickLevel.PRO -> poolPro.remove(trickActual)
                }

                _uiState.update {
                    it.copy(
                        cadenaActiva = true,
                        jugadoresQueYaIntentaronEsteTruco = it.jugadoresQueYaIntentaronEsteTruco + jugadorActual.id
                    )
                }
                avanzarSiguienteJugadorDeCadenaOFinRonda()
            }
        }
    }

    private fun registrarResultadoCadena(
        jugadorActual: Player,
        resultado: TurnResult
    ) {
        val jugadorActualizado = when (resultado) {
            TurnResult.CHECK -> jugadorActual
            TurnResult.FAIL -> ganarLetra(jugadorActual)
            TurnResult.PASAR -> jugadorActual // no debería ocurrir, PASAR no está disponible en cadena
        }

        actualizarJugador(jugadorActualizado)

        _uiState.update {
            it.copy(
                jugadoresQueYaIntentaronEsteTruco = it.jugadoresQueYaIntentaronEsteTruco + jugadorActual.id
            )
        }

        avanzarSiguienteJugadorDeCadenaOFinRonda()
    }

    private fun actualizarJugador(jugadorActualizado: Player) {
        _uiState.update { state ->
            val jugadorAntes = state.jugadores.find { it.id == jugadorActualizado.id }
            val recienEliminado = jugadorAntes?.eliminado == false && jugadorActualizado.eliminado

            state.copy(
                jugadores = state.jugadores.map {
                    if (it.id == jugadorActualizado.id) jugadorActualizado else it
                },
                jugadorEliminadoAviso = if (recienEliminado) jugadorActualizado else state.jugadorEliminadoAviso
            )
        }
    }
    fun descartarAvisoEliminacion() {
        _uiState.update { it.copy(jugadorEliminadoAviso = null) }
    }
    /**
     * El iniciador hizo PASAR o FAIL: la ronda termina sin cadena.
     * El siguiente jugador vivo se convierte en el nuevo iniciador.
     */
    private fun cerrarRondaSinCadena() {
        val vivos = _uiState.value.jugadores.filter { !it.eliminado }

        if (comprobarFinDePartidaOMuerteSubita(vivos)) return

        avanzarIniciadorDesdeIniciadorDeRonda()
        _uiState.update {
            it.copy(
                truckActualDelTurno = null,
                cadenaActiva = false,
                jugadoresQueYaIntentaronEsteTruco = emptySet()
            )
        }
        verificarTrucosDisponibles()
    }

    /**
     * Avanza al siguiente jugador vivo dentro de la cadena.
     * Si ya la han intentado todos los vivos, cierra la ronda.
     */
    private fun avanzarSiguienteJugadorDeCadenaOFinRonda() {
        val state = _uiState.value
        val vivos = state.jugadores.filter { !it.eliminado }

        if (comprobarFinDePartidaOMuerteSubita(vivos)) return

        val pendientes = vivos.filter { it.id !in state.jugadoresQueYaIntentaronEsteTruco }

        if (pendientes.isEmpty()) {
            // Cadena completada: cierra la ronda, el siguiente iniciador
            // es el jugador vivo siguiente al que abrió esta ronda
            avanzarIniciadorDesdeIniciadorDeRonda()
            _uiState.update {
                it.copy(
                    truckActualDelTurno = null,
                    cadenaActiva = false,
                    jugadoresQueYaIntentaronEsteTruco = emptySet()
                )
            }
            verificarTrucosDisponibles()
        } else {
            val siguiente = pendientes.first()
            val siguienteIndex = state.jugadores.indexOfFirst { it.id == siguiente.id }
            _uiState.update { it.copy(turnoActualIndex = siguienteIndex) }
        }
    }

    private fun avanzarIniciador(indexIniciadorActual: Int) {
        val state = _uiState.value
        var siguienteIndex = indexIniciadorActual
        do {
            siguienteIndex = (siguienteIndex + 1) % state.jugadores.size
        } while (state.jugadores[siguienteIndex].eliminado)

        iniciadorRondaActualId = state.jugadores[siguienteIndex].id
        _uiState.update { it.copy(turnoActualIndex = siguienteIndex) }
    }

    private fun avanzarIniciadorDesdeIniciadorDeRonda() {
        val state = _uiState.value
        val idIniciador = iniciadorRondaActualId ?: state.jugadores[state.turnoActualIndex].id
        val indexIniciador = state.jugadores.indexOfFirst { it.id == idIniciador }
        avanzarIniciador(indexIniciador)
    }

    private fun comprobarFinDePartidaOMuerteSubita(vivos: List<Player>): Boolean {
        if (vivos.size == 1) {
            _uiState.update { it.copy(estado = GameState.FINISHED, ganador = vivos.first()) }
            return true
        }
        if (vivos.isEmpty() && _uiState.value.estado != GameState.SUDDEN_DEATH) {
            iniciarMuerteSubita()
            return true
        }
        if (_uiState.value.estado == GameState.SUDDEN_DEATH) {
            return resolverMuerteSubitaSiCorresponde(vivos)
        }
        return false
    }

    private fun iniciarMuerteSubita() {
        val jugadoresRevividos = _uiState.value.jugadores.map {
            if (it.letrasAcumuladas >= 5) it.copy(eliminado = false) else it
        }
        _uiState.update {
            it.copy(
                jugadores = jugadoresRevividos,
                estado = GameState.SUDDEN_DEATH,
                truckActualDelTurno = null,
                cadenaActiva = false,
                jugadoresQueYaIntentaronEsteTruco = emptySet()
            )
        }
    }

    private fun resolverMuerteSubitaSiCorresponde(vivos: List<Player>): Boolean {
        if (vivos.size == 1) {
            _uiState.update { it.copy(estado = GameState.FINISHED, ganador = vivos.first()) }
            return true
        }
        return false
    }
}