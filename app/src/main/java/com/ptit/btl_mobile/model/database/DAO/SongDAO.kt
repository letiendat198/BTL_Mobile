package com.ptit.btl_mobile.model.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ptit.btl_mobile.model.database.Song
import com.ptit.btl_mobile.model.database.SongArtistCrossRef
import com.ptit.btl_mobile.model.database.SongWithArtists

@Dao
interface SongDAO {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(vararg songs: Song): List<Long>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSong(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSongWithArtists(ref: SongArtistCrossRef)

    @Delete
    suspend fun delete(song: Song)

    @Query("SELECT * FROM Song")
    suspend fun getAll(): List<Song>

    @Transaction
    @Query("SELECT * FROM Song")
    suspend fun getAllWithArtists(): List<SongWithArtists>

    @Transaction
    @Query("""
        SELECT * FROM Song 
        INNER JOIN PlaylistSongCrossRef ON Song.songId = PlaylistSongCrossRef.songId
        WHERE PlaylistSongCrossRef.playlistId = :playlistId
    """)
    suspend fun getSongsByPlaylistId(playlistId: Long): List<SongWithArtists>
}