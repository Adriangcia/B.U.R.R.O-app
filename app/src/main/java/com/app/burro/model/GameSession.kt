package com.app.burro.model

enum class GameMode {
    STANDARD,
    BASIC,
    MEDIUM,
    PRO
}

enum class GameState {
    NORMAL,
    SUDDEN_DEATH,
    FINISHED
}

data class GameSession(
    val modo: GameMode,
    val jugadores: List<Player>,
    val turnoActualIndex: Int = 0,
    val contadorTrucosGlobal: Int = 0,
    val truckActualDelTurno: Trick? = null,
    val jugadoresQueYaIntentaronEsteTruco: Set<Long> = emptySet(),
    val trucosYaUsadosIds: Set<Long> = emptySet(),
    val estado: GameState = GameState.NORMAL
)