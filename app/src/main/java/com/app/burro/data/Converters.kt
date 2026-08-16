package com.app.burro.data

import androidx.room.TypeConverter
import com.app.burro.model.TrickLevel

class Converters {
    @TypeConverter
    fun fromTrickLevel(nivel: TrickLevel): String {
        return nivel.name
    }

    @TypeConverter
    fun toTrickLevel(nivel: String): TrickLevel {
        return TrickLevel.valueOf(nivel)
    }
}