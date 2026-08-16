package com.app.burro.model

data class Player(
    val id: Long = 0,
    val nombre: String,
    val letrasAcumuladas: Int = 0,
    val pasesEnLetraActual: Int = 0,
    val eliminado: Boolean = false
)