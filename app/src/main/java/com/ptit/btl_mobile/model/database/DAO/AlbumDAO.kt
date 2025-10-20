package com.ptit.btl_mobile.model.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ptit.btl_mobile.model.database.Album
import com.ptit.btl_mobile.model.database.AlbumArtistCrossRef
import com.ptit.btl_mobile.model.database.AlbumWithArtists
import com.ptit.btl_mobile.model.database.AlbumWithSongs

@Dao
interface AlbumDAO {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(vararg albums: Album): List<Long>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAlbum(album: Album): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAlbumWithArtists(ref: AlbumArtistCrossRef)

    @Delete
    suspend fun delete(album: Album)

    @Query("SELECT * FROM Album")
    suspend fun getAll(): List<Album>

    @Transaction
    @Query("SELECT * FROM Album")
    suspend fun getAllWithArtists(): List<AlbumWithArtists>

    @Transaction
    @Query("SELECT * FROM Album")
    suspend fun getAllWithSongs(): List<AlbumWithSongs>
}