package com.ptit.btl_mobile.model.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ptit.btl_mobile.model.database.Playlist
import com.ptit.btl_mobile.model.database.PlaylistSongCrossRef
import com.ptit.btl_mobile.model.database.PlaylistWithSongs

@Dao
interface PlaylistDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg playlists: Playlist): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistWithSongs(ref: PlaylistSongCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongsToPlaylist(crossRefs: List<PlaylistSongCrossRef>)

    @Query("SELECT * FROM Playlist ORDER BY dateCreated DESC")
    suspend fun getAll(): List<Playlist>

    @Transaction
    @Query("SELECT * FROM Playlist ORDER BY dateCreated DESC")
    suspend fun getAllWithSongs(): List<PlaylistWithSongs>

    @Query("SELECT * FROM Playlist WHERE playlistId = :id")
    suspend fun getById(id: Long): Playlist?

    @Update
    suspend fun update(playlist: Playlist)

    @Delete
    suspend fun delete(playlist: Playlist)

    @Query("DELETE FROM Playlist WHERE playlistId = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM PlaylistSongCrossRef WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deleteSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM PlaylistSongCrossRef WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)
}
