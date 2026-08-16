package com.app.burro.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TrickLevel {
    BASIC,
    MEDIUM,
    PRO
}

@Entity(tableName = "tricks")
data class Trick(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val nivel: TrickLevel,
    val sublevel: Int
)