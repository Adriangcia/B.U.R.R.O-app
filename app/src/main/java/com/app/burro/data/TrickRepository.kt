package com.app.burro.data

import android.content.Context
import com.app.burro.model.Trick
import com.app.burro.model.TrickLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TrickJson(
    val nombre: String,
    val nivel: String,
    val sublevel: Int
)

class TrickRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.trickDao()

    suspend fun getAllTricks(): List<Trick> {
        seedIfEmpty()
        return dao.getAll()
    }

    suspend fun getTricksByNivel(nivel: TrickLevel): List<Trick> {
        seedIfEmpty()
        return dao.getByNivel(nivel)
    }

    private suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            val jsonText = context.assets.open("tricks.json")
                .bufferedReader()
                .use { it.readText() }

            val parsedList = Json.decodeFromString<List<TrickJson>>(jsonText)

            val tricks = parsedList.map {
                Trick(
                    nombre = it.nombre,
                    nivel = TrickLevel.valueOf(it.nivel),
                    sublevel = it.sublevel
                )
            }

            dao.insertAll(tricks)
        }
    }
}