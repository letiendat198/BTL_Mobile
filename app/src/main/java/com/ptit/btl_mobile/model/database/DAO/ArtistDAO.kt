package com.ptit.btl_mobile.model.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ptit.btl_mobile.model.database.Artist

@Dao
interface ArtistDAO {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(vararg artists: Artist): List<Long>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertArtist(artist: Artist): Long

    @Delete
    suspend fun delete(artist: Artist)

    @Query("SELECT * FROM Artist")
    suspend fun getAll(): List<Artist>
}