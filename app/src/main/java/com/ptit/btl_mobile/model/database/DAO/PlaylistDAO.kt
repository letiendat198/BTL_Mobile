package com.ptit.btl_mobile.model.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ptit.btl_mobile.model.database.Playlist
import com.ptit.btl_mobile.model.database.PlaylistSongCrossRef
import com.ptit.btl_mobile.model.database.PlaylistWithSongs

@Dao
interface PlaylistDAO {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(vararg playlists: Playlist): List<Long>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertPlaylistWithSongs(ref: PlaylistSongCrossRef)

    @Delete
    suspend fun delete(playlist: Playlist)

    @Query("SELECT * FROM Playlist")
    suspend fun getAll(): List<Playlist>

    @Transaction
    @Query("SELECT * FROM Playlist")
    suspend fun getAllWithSongs(): List<PlaylistWithSongs>
}