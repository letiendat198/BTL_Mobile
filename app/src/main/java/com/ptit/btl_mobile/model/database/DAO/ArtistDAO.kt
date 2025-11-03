package com.ptit.btl_mobile.model.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ptit.btl_mobile.model.database.Artist
import com.ptit.btl_mobile.model.database.Song
import com.ptit.btl_mobile.model.database.SongWithArtists

@Dao
interface ArtistDAO {
    // Name may collide. Replace will change PK which is not good => Ignore
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertAll(vararg artists: Artist): List<Long>

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertArtist(artist: Artist): Long

    @Delete
    suspend fun delete(artist: Artist)

    @Query("SELECT * FROM Artist WHERE name = :name")
    suspend fun searchArtistByName(name: String): List<Artist>

    suspend fun safeInsertArtist(artist: Artist): Long {
        val artists = searchArtistByName(artist.name)
        if (artists.isNotEmpty()) return artists[0].artistId // Normally artist name won't duplicate
        else return insertArtist(artist)
    }
    @Query("SELECT * FROM Artist ORDER BY name ASC")
    suspend fun getAll(): List<Artist>

    @Query("SELECT * FROM Artist WHERE artistId = :artistId")
    suspend fun getById(artistId: Long): Artist?

    @Query("""
        SELECT s.* FROM Song s
        JOIN SongArtistCrossRef sac ON s.songId = sac.songId
        WHERE sac.artistId = :artistId
        ORDER BY s.name ASC
    """)
    suspend fun getSongsByArtistId(artistId: Long): List<SongWithArtists>

    // Query để lấy số lượng album của artist
    @Query("""
        SELECT COUNT(DISTINCT aac.albumId) 
        FROM AlbumArtistCrossRef aac 
        WHERE aac.artistId = :artistId
    """)
    suspend fun getAlbumCountByArtistId(artistId: Long): Int

    // Query để lấy số lượng bài hát của artist
    @Query("""
        SELECT COUNT(DISTINCT sac.songId) 
        FROM SongArtistCrossRef sac 
        WHERE sac.artistId = :artistId
    """)
    suspend fun getSongCountByArtistId(artistId: Long): Int
}