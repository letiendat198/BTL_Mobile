package com.ptit.btl_mobile.model.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ptit.btl_mobile.model.database.Song
import com.ptit.btl_mobile.model.database.SongArtistCrossRef
import com.ptit.btl_mobile.model.database.SongWithAlbum
import com.ptit.btl_mobile.model.database.SongWithArtists

@Dao
interface SongDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg songs: Song): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongWithArtists(ref: SongArtistCrossRef)

    @Update
    suspend fun update(song: Song)

    @Delete
    suspend fun delete(song: Song)

    @Query("SELECT * FROM Song")
    suspend fun getAll(): List<Song>

    @Transaction
    @Query("SELECT * FROM Song")
    suspend fun getAllWithArtists(): List<SongWithArtists>

    @Transaction
    @Query("SELECT * FROM Song")
    suspend fun getAllWithAlbum(): List<SongWithAlbum>

    @Transaction
    @Query("SELECT * FROM Song WHERE songId = :songId")
    suspend fun getSongWithAlbum(songId: Long): SongWithAlbum

    @Transaction
    @Query("SELECT * FROM Song WHERE songId = :songId")
    suspend fun getSongById(songId: Long): Song?

    @Transaction
    @Query("SELECT * FROM Song ORDER BY dateAdded DESC LIMIT :limit")
    suspend fun getRecentlyAdded(limit: Int): List<SongWithArtists>

    @Transaction
    @Query("""
        SELECT * FROM Song 
        INNER JOIN PlaylistSongCrossRef ON Song.songId = PlaylistSongCrossRef.songId
        WHERE PlaylistSongCrossRef.playlistId = :playlistId
    """)
    suspend fun getSongsByPlaylistId(playlistId: Long): List<SongWithArtists>

    @Query("UPDATE Song SET lyricUri = :lyricUri WHERE songId = :songId")
    suspend fun updateLyricUri(songId: Long, lyricUri: String?)
}