package com.app.burro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.burro.model.Trick
import com.app.burro.model.TrickLevel

@Dao
interface TrickDao {

    @Query("SELECT * FROM tricks")
    suspend fun getAll(): List<Trick>

    @Query("SELECT * FROM tricks WHERE nivel = :nivel")
    suspend fun getByNivel(nivel: TrickLevel): List<Trick>

    @Query("SELECT COUNT(*) FROM tricks")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tricks: List<Trick>)

    @Insert
    suspend fun insert(trick: Trick)
}