package com.ptit.btl_mobile.model.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Date

@Entity
data class Song (
    @PrimaryKey(autoGenerate = true) val songId: Long = 0,
    val name: String,
    val songUri: String,
    val duration: Int,
    val dateAdded: Long,
    val imageUri: String? = null,
    val songAlbumId: Long? = null,
)

// Artist name should be unique. Otherwise, there's no way to tell two artist apart
@Entity(indices = [Index(value = ["name"], unique = true)])
data class Artist (
    @PrimaryKey (autoGenerate = true) val artistId: Long = 0,
    val name: String,
    val description: String? = null,
    val imageUri: String? = null
)

// Album name should be unique. Otherwise, there's no way to tell two album apart
@Entity(indices = [Index(value = ["name"], unique = true)])
data class Album(
    @PrimaryKey(autoGenerate = true) val albumId: Long = 0,
    val name: String,
    val genre: String,
    val imageUri: String? = null,
    val year: Int,
)

@Entity
data class Playlist (
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    val name: String,
    val imageUri: String? = null,
    val dateCreated: Date
)

@Entity(primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long
)

@Entity(primaryKeys = ["artistId", "songId"])
data class SongArtistCrossRef(
    val artistId: Long,
    val songId: Long
)

@Entity(primaryKeys = ["artistId", "albumId"])
data class AlbumArtistCrossRef(
    val artistId: Long,
    val albumId: Long
)

data class AlbumWithSongs(
    @Embedded val album: Album,
    @Relation(
        parentColumn = "albumId",
        entityColumn = "songAlbumId"
    )
    val songs: List<Song>
)

data class PlaylistWithSongs (
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "songId",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val songs: List<Song>
)

data class SongWithArtists(
    @Embedded val song: Song,
    @Relation(
        parentColumn = "songId",
        entityColumn = "artistId",
        associateBy = Junction(SongArtistCrossRef::class)
    )
    val artists: List<Artist>
)

data class AlbumWithArtists(
    @Embedded val song: Album,
    @Relation(
        parentColumn = "albumId",
        entityColumn = "artistId",
        associateBy = Junction(AlbumArtistCrossRef::class)
    )
    val artists: List<Artist>
)