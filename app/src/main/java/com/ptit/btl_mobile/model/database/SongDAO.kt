package com.ptit.btl_mobile.model.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SongDAO {
    @Insert
    suspend fun insertAll(vararg songs: Song)

    @Delete
    suspend fun delete(song: Song)

    @Query("SELECT * FROM Song")
    suspend fun getAll(): List<Song>
}