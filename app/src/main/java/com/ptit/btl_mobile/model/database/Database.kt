package com.ptit.btl_mobile.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ptit.btl_mobile.model.database.DAO.SongDAO
import com.ptit.btl_mobile.util.DateConverter

@Database(entities = [
    Song::class,
    Playlist::class,
    Artist::class,
    Album::class,
    SongArtistCrossRef::class,
    AlbumArtistCrossRef::class,
    PlaylistSongCrossRef::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class PlayerDatabase: RoomDatabase() {
    abstract fun SongDAO(): SongDAO
}

class Database(context: Context) {
    companion object {
        var db: PlayerDatabase? = null
        public fun getInstance(): PlayerDatabase =
            db ?: error("Database must be initialized before use")
    }
    init {
        db = Room.databaseBuilder(context,
            PlayerDatabase::class.java, "player_database").build()
    }
}