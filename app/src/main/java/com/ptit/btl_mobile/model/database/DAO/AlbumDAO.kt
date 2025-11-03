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
import com.ptit.btl_mobile.model.database.Artist
import com.ptit.btl_mobile.model.database.Song
import com.ptit.btl_mobile.model.database.SongWithArtists

@Dao
interface AlbumDAO {
    // Name may collide. Replace will change PK which is not good => Ignore
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertAll(vararg albums: Album): List<Long>

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertAlbum(album: Album): Long

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertAlbumWithArtists(ref: AlbumArtistCrossRef)

    suspend fun safeInsertAlbum(album: Album): Long {
        val albums = getAlbumsByName(album.name)
        if (albums.isNotEmpty()) return albums[0].albumId // Normally artist name won't duplicate
        else return insertAlbum(album)
    }

    @Delete
    suspend fun delete(album: Album)

    @Transaction
    @Query("SELECT * FROM Album")
    suspend fun getAllWithArtists(): List<AlbumWithArtists>

    @Transaction
    @Query("SELECT * FROM Album")
    suspend fun getAllWithSongs(): List<AlbumWithSongs>

    @Query("SELECT * FROM Album ORDER BY name ASC")
    suspend fun getAll(): List<Album>

    @Query("SELECT * FROM Album WHERE albumId = :albumId")
    suspend fun getById(albumId: Long): Album?

    @Query("""
        SELECT a.* FROM Album a
        JOIN AlbumArtistCrossRef aac ON a.albumId = aac.albumId
        WHERE aac.artistId = :artistId
        ORDER BY a.year DESC, a.name ASC
    """)
    suspend fun getByArtistId(artistId: Long): List<Album>

    @Query("SELECT * FROM Song WHERE songAlbumId = :albumId ORDER BY name ASC")
    suspend fun getSongsByAlbumId(albumId: Long): List<SongWithArtists>

    // Query để lấy số lượng bài hát trong album
    @Query("SELECT COUNT(*) FROM Song WHERE songAlbumId = :albumId")
    suspend fun getSongCountByAlbumId(albumId: Long): Int

    // Query để lấy tên artist đầu tiên của album
    @Query("""
        SELECT ar.name FROM Artist ar
        JOIN AlbumArtistCrossRef aac ON ar.artistId = aac.artistId
        WHERE aac.albumId = :albumId
        LIMIT 1
    """)
    suspend fun getFirstArtistNameByAlbumId(albumId: Long): String?

    @Transaction
    @Query("SELECT * FROM Album WHERE albumId = :albumId")
    suspend fun getAlbumWithSongs(albumId: Long): AlbumWithSongs?

    @Query("SELECT * FROM Album WHERE name = :name")
    suspend fun getAlbumsByName(name: String): List<Album>
}