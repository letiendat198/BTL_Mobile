package com.ptit.btl_mobile.model.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Date

@Entity
data class Song (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val songString: String?,
    val duration: Long,
    val dateAdded: Date,
    val imageString: String?,
    val playlistId: Int?,
    val albumId: Int?,
)

@Entity
data class Artist (
    @PrimaryKey (autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val imageString: String?
)

@Entity
data class Album(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val genre: String,
    val imageString: String?,
    val year: Int,
)

@Entity
data class Playlist (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val imageString: String?,
    val dateCreated: Date
)

data class PlaylistWithSongs (
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId"
    )
    val songs: List<Song>
)