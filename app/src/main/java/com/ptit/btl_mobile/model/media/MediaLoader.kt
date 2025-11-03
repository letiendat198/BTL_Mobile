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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date

class MediaLoader(val context: Context, val scope: LifecycleCoroutineScope) {
    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 1)
    fun updateOrReloadMedia() {
        scope.launch {
//            val currentVersion = MediaStore.getVersion(context)
//            val currentGeneration = MediaStore.getGeneration(context, MediaStore.VOLUME_EXTERNAL)
//
//            val cachedVersion = context.dataStore.data.map { value ->
//                value[PreferencesKeys.MEDIASTORE_VERSION]
//            }.firstOrNull()
//            val cachedGeneration= context.dataStore.data.map { value ->
//                value[PreferencesKeys.MEDIASTORE_GENERATION]
//            }.firstOrNull()?:0
//
//            Log.d("MEDIA_LOADER", "Cached version: $cachedVersion. Current version: $currentVersion")
//            Log.d("MEDIA_LOADER", "Cached generation: $cachedGeneration. Current generation: $currentGeneration")
//
//
//            if (currentVersion == cachedVersion) {
//                if (currentGeneration > cachedGeneration) loadMediaIntoDB(cachedGeneration)
//            }
//            else loadMediaIntoDB()
//
//            context.dataStore.edit { values ->
//                values[PreferencesKeys.MEDIASTORE_VERSION] = currentVersion
//            }
//
//            context.dataStore.edit { values ->
//                values[PreferencesKeys.MEDIASTORE_GENERATION] = currentGeneration
//            }
            loadMediaIntoDB()

            val currentVersion = MediaStore.getVersion(context)
            val currentGeneration = MediaStore.getGeneration(context, MediaStore.VOLUME_EXTERNAL)

            context.dataStore.edit { values ->
                values[PreferencesKeys.MEDIASTORE_VERSION] = currentVersion
                values[PreferencesKeys.MEDIASTORE_GENERATION] = currentGeneration
            }
        }
    }

    // Either query all songs or songs added after <generation>
//    fun loadMediaIntoDB(generation: Long? = null) {
//        Toast.makeText(context, "Loading media on device...", Toast.LENGTH_LONG).show()
//        scope.launch(Dispatchers.IO) {
//            Log.d("MEDIA_LOADER", "Trying to load media...")
//
//            val selection = generation?.let { "${MediaStore.Audio.Media.GENERATION_ADDED} > ?" }
//            val selectionArgs = generation?.let { arrayOf(generation.toString()) }
//
//            val db = Database.getInstance()
//            context.contentResolver.query(
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                null,
//                selection,
//                selectionArgs,
//                null
//            )?.use { cursor ->
//                while (cursor.moveToNext()) {
//                    var songWithArtists = readFromStoreToSong(cursor)
//                    val albumWithArtists = readFromStoreToAlbum(cursor)
//
//                    // If there's album info
//                    if (albumWithArtists != null) {
//                        // Add album and album artists to db
//                        val albumId = db.AlbumDAO().safeInsertAlbum(albumWithArtists.album)
//                        albumWithArtists.artists.forEach { artist ->
//                            // Using non-safe insert won't return duplicated artist id
//                            val artistId = db.ArtistDAO().safeInsertArtist(artist)
//                            db.AlbumDAO().insertAlbumWithArtists(AlbumArtistCrossRef(artistId, albumId))
//                        }
//
//                        // Inefficient stuff to add albumId into song
//                        songWithArtists = songWithArtists.copy(song = songWithArtists.song.copy(
//                            songAlbumId = albumId
//                        ))
//                    }
//
//                    // Add song and song artists to db
//                    val songId = db.SongDAO().insertSong(songWithArtists.song)
//                    songWithArtists.artists.forEach { artist ->
//                        val artistId = db.ArtistDAO().safeInsertArtist(artist)
//                        db.SongDAO().insertSongWithArtists(SongArtistCrossRef(artistId, songId))
//                    }
//                }
//            }
//        }.invokeOnCompletion {
//            scope.launch(Dispatchers.Main) {
//                Toast.makeText(context, "Media loading completed!", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    fun loadMediaIntoDB(generation: Long? = null) {
        Toast.makeText(context, "Loading media on device...", Toast.LENGTH_LONG).show()
        scope.launch(Dispatchers.IO) {
            Log.d("MEDIA_LOADER", "Trying to load media...")

            // Scan file thủ công và chờ hoàn thành
            val musicDir = File("/sdcard/Music")
            if (musicDir.exists()) {
                val files = musicDir.listFiles()?.filter {
                    it.extension in listOf("mp3", "wav", "m4a")
                } ?: emptyList()

                if (files.isNotEmpty()) {
                    // Chờ scan hoàn thành
                    withContext(Dispatchers.Main) {
                        var scannedCount = 0
                        files.forEach { file ->
                            android.media.MediaScannerConnection.scanFile(
                                context,
                                arrayOf(file.absolutePath),
                                null
                            ) { path, uri ->
                                Log.d("MEDIA_LOADER", "Scanned file: $path -> $uri")
                                scannedCount++
                            }
                        }
                    }
                    // Chờ một chút để MediaStore cập nhật
                    kotlinx.coroutines.delay(2000)
                }
            }

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
                Log.d("MEDIA_LOADER", "Found ${cursor.count} songs")
                while (cursor.moveToNext()) {
                    var songWithArtists = readFromStoreToSong(cursor)
                    val albumWithArtists = readFromStoreToAlbum(cursor)

                    // If there's album info
                    if (albumWithArtists != null) {
                        // Add album and album artists to db
                        val albumId = db.AlbumDAO().safeInsertAlbum(albumWithArtists.album)
                        albumWithArtists.artists.forEach { artist ->
                            // Using non-safe insert won't return duplicated artist id
                            val artistId = db.ArtistDAO().safeInsertArtist(artist)
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