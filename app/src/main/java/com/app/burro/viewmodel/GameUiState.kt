package com.app.burro.viewmodel

import com.app.burro.model.GameMode
import com.app.burro.model.GameState
import com.app.burro.model.Player
import com.app.burro.model.Trick

data class GameUiState(
    val modo: GameMode = GameMode.STANDARD,
    val jugadores: List<Player> = emptyList(),
    val turnoActualIndex: Int = 0,
    val contadorTrucosGlobal: Int = 0,
    val truckActualDelTurno: Trick? = null,
    val cadenaActiva: Boolean = false,
    val jugadoresQueYaIntentaronEsteTruco: Set<Long> = emptySet(),
    val trucosYaUsadosIds: Set<Long> = emptySet(),
    val estado: GameState = GameState.NORMAL,
    val ganador: Player? = null
) {
    val jugadorActual: Player?
        get() = jugadores.getOrNull(turnoActualIndex)

    val jugadoresVivos: List<Player>
        get() = jugadores.filter { !it.eliminado }

    val esIniciador: Boolean
        get() = !cadenaActiva
}