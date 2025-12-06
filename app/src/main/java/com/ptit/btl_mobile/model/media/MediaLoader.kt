package com.ptit.btl_mobile.model.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.core.database.getStringOrNull
import androidx.lifecycle.LifecycleCoroutineScope
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import com.ptit.btl_mobile.dataStore
import com.ptit.btl_mobile.model.database.Album
import com.ptit.btl_mobile.model.database.AlbumArtistCrossRef
import com.ptit.btl_mobile.model.database.AlbumWithArtists
import com.ptit.btl_mobile.model.database.Artist
import com.ptit.btl_mobile.model.database.SongArtistCrossRef
import com.ptit.btl_mobile.model.database.SongWithArtists
import com.ptit.btl_mobile.model.datastore.PreferencesKeys
import com.ptit.btl_mobile.util.DateConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date

class MediaLoader(val context: Context, val scope: CoroutineScope) {
    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 1)
    fun updateOrReloadMedia() {
        scope.launch {
            val currentVersion = MediaStore.getVersion(context)
            val currentGeneration = MediaStore.getGeneration(context, MediaStore.VOLUME_EXTERNAL)

            val cachedVersion = context.dataStore.data.map { value ->
                value[PreferencesKeys.MEDIASTORE_VERSION]
            }.firstOrNull()
            val cachedGeneration= context.dataStore.data.map { value ->
                value[PreferencesKeys.MEDIASTORE_GENERATION]
            }.firstOrNull()?:0

            Log.d("MEDIA_LOADER", "Cached version: $cachedVersion. Current version: $currentVersion")
            Log.d("MEDIA_LOADER", "Cached generation: $cachedGeneration. Current generation: $currentGeneration")


            if (currentVersion == cachedVersion) {
                if (currentGeneration > cachedGeneration) loadMediaIntoDB(cachedGeneration)
            }
            else loadMediaIntoDB()

            context.dataStore.edit { values ->
                values[PreferencesKeys.MEDIASTORE_VERSION] = currentVersion
            }

            context.dataStore.edit { values ->
                values[PreferencesKeys.MEDIASTORE_GENERATION] = currentGeneration
            }
        }
    }

    // Either query all songs or songs added after <generation>
    fun loadMediaIntoDB(generation: Long? = null) {
        Toast.makeText(context, "Loading media on device...", Toast.LENGTH_LONG).show()
        scope.launch(Dispatchers.IO) {
            Log.d("MEDIA_LOADER", "Trying to load media...")

            val selection = generation?.let { "${MediaStore.Audio.Media.GENERATION_ADDED} > ?" }
            val selectionArgs = generation?.let { arrayOf(generation.toString()) }

            val db = Database.getInstance()
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val indexIsMusic = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
                    val isMusic = cursor.getInt(indexIsMusic)
                    if (isMusic == 0) continue

                    var songWithArtists = readFromStoreToSong(cursor)
                    var albumWithArtists = readFromStoreToAlbum(cursor)

                    // If there's album info
                    if (albumWithArtists != null) {
                        // Set album image as its first song
                        albumWithArtists = albumWithArtists.copy(
                            album = albumWithArtists.album.copy(imageUri = songWithArtists.song.imageUri)
                        )
                        // Add album and album artists to db
                        val albumId = db.AlbumDAO().safeInsertAlbum(albumWithArtists.album)
                        albumWithArtists.artists.forEach { artist ->
                            val artistWithImage = artist.copy(imageUri = songWithArtists.song.imageUri)
                            // Using non-safe insert won't return duplicated artist id
                            val artistId = db.ArtistDAO().safeInsertArtist(artistWithImage)
                            db.AlbumDAO().insertAlbumWithArtists(AlbumArtistCrossRef(artistId, albumId))
                        }

                        // Inefficient stuff to add albumId into song
                        songWithArtists = songWithArtists.copy(song = songWithArtists.song.copy(
                            songAlbumId = albumId
                        ))
                    }

                    // Add song and song artists to db
                    val songId = db.SongDAO().insertSong(songWithArtists.song)
                    songWithArtists.artists.forEach { artist ->
                        val artistId = db.ArtistDAO().safeInsertArtist(artist)
                        db.SongDAO().insertSongWithArtists(SongArtistCrossRef(artistId, songId))
                    }
                }
            }
        }.invokeOnCompletion {
            scope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Media loading completed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Would be slow AF
    // Update: Turns out it's not that slow :D
    public fun cleanUpSong() {
        Log.d("MEDIA_LOADER","Clean up called")
        scope.launch(Dispatchers.IO) {
            val db = Database.getInstance()
            val songs = db.SongDAO().getAllWithArtists()
            val contentResolver = context.contentResolver
            songs.forEach { (song, artists) ->
                var shouldDelete = false
                contentResolver.query(
                    Uri.parse(song.songUri),
                    arrayOf(MediaStore.MediaColumns.DATA),
                    null,
                    null,
                    null,
                    null
                ).use { cursor ->
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            val path = cursor.getString(0)
                            if (!File(path).exists()) shouldDelete = true
                        }
                        else {
                            shouldDelete = true
                        }
                    }
                    else { // Content URI unavailable -> Delete
                        shouldDelete = true
                    }
                }

                if (shouldDelete) {
                    Log.d("MEDIA_LOADER", "Cleaned up song ${song.name}")
                    // If artist has no song -> Remove
                    artists.forEach { artist ->
                        val songs = db.ArtistDAO().getSongsByArtistId(artist.artistId)
                        if (songs.size <= 1) {
                            db.ArtistDAO().delete(artist)
                        }
                    }
                    // If album has no song -> Remove
                    val album = db.SongDAO().getSongWithAlbum(song.songId)
                    // If song actually have album
                    if (album.album != null) {
                        val albumSongs = db.AlbumDAO().getAlbumWithSongs(album.album.albumId)

                        if (albumSongs == null || albumSongs.songs.size <= 1) {
                            db.AlbumDAO().delete(album.album)
                        }
                    }
                    // Delete song last, otherwise crash
                    db.SongDAO().delete(song)
                }
            }
        }
    }

    private fun readFromStoreToSong(cursor: Cursor): SongWithArtists {
        val indexId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val indexName = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val indexDuration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val indexDateAdded = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

        val indexArtist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
//        Log.d("MEDIA_LOADER", MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString());

        val id = cursor.getLong(indexId)
        val name = cursor.getString(indexName)
        val duration = cursor.getLong(indexDuration)
        val dateAdded = cursor.getLong(indexDateAdded)
        val artistName = cursor.getStringOrNull(indexArtist)

        Log.d("MEDIA_LOADER", id.toString())
        Log.d("MEDIA_LOADER", "$name")
        Log.d("MEDIA_LOADER", "$artistName")

        val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

        val imageUri = copyThumbnailToInternal(contentUri, "song$id")

        val song = Song(
            songId = id,
            name = name,
            songUri = contentUri.toString(),
            duration = duration,
            dateAdded = dateAdded,
            imageUri = imageUri?.toString()
        )

        val listArtists = mutableListOf<Artist>()

        if (!artistName.isNullOrEmpty()) {
            val artists = artistName.split(", ")
            artists.forEach { i ->
                val artist = Artist(
                    name = artistName,
                    imageUri = imageUri?.toString() // Artist share same image as their first song
                )
                listArtists.add(artist)
            }
        }

        val songWithArtists = SongWithArtists(
            song = song,
            artists = listArtists
        )

        return songWithArtists
    }


    fun readFromStoreToAlbum(cursor: Cursor): AlbumWithArtists? {
        val indexAlbum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val indexAlbumArtist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)

        val albumName = cursor.getStringOrNull(indexAlbum)
        val albumArtistsName = cursor.getStringOrNull(indexAlbumArtist)

        Log.d("MEDIA_LOADER", "$albumName")
        Log.d("MEDIA_LOADER", "$albumArtistsName")

        if (!albumName.isNullOrEmpty()) {
            val album = Album(
                name = albumName
            )

            val listAlbumArtists = mutableListOf<Artist>()
            if (!albumArtistsName.isNullOrEmpty()) {
                val artists = albumArtistsName.split(", ")
                artists.forEach { i ->
                    val artist = Artist(
                        name = albumArtistsName,
                    )
                    listAlbumArtists.add(artist)
                }
            }

            return AlbumWithArtists(
                album = album,
                artists = listAlbumArtists
            )
        }
        else return null
    }

    fun copyThumbnailToInternal(contentUri: Uri, filename: String): Uri? {
        try {
            val thumbnail: Bitmap = context.contentResolver.loadThumbnail(
                contentUri,
                Size(800, 800),
                null
            )
            val thumbnailPath = "images/$filename.png"
            val imageUri = writeBitmapToInternal(thumbnail, thumbnailPath)
            return imageUri
        }
        catch (e: FileNotFoundException) {
            Log.d("MEDIA_LOADER", "No thumbnail found for uri: $contentUri")
            return null
        }
    }

    fun writeBitmapToInternal(bitmap: Bitmap, path: String): Uri {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()

        val file = File(context.filesDir, path)
        file.parentFile?.mkdir()
        file.createNewFile()
        file.writeBytes(bytes)

        return Uri.fromFile(file)
    }


}