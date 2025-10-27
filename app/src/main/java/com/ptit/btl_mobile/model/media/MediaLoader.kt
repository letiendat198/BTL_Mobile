package com.ptit.btl_mobile.model.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.lifecycle.LifecycleCoroutineScope
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import androidx.core.net.toUri
import com.ptit.btl_mobile.model.database.SongArtistCrossRef

class MediaLoader(val context: Context, val scope: LifecycleCoroutineScope) {
    fun loadMediaIntoDB() {
        scope.launch(Dispatchers.IO) {
            Log.d("MEDIA_LOADER", "Trying to load media...")
            val db = Database.getInstance()
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val song = readFromStoreToSong(cursor)
                    val songId = db.SongDAO().insertSong(song)

                    context.contentResolver.openFileDescriptor(song.songUri.toUri(), "r").use { pfd ->
                        if (pfd != null){
                            val artists = MetadataHelper.readArtist(pfd.dup().detachFd())


                            artists?.forEach { artist ->
                                val artistId = db.ArtistDAO().safeInsertArtist(artist) // May not need safe
                                db.SongDAO().insertSongWithArtists(SongArtistCrossRef(artistId, songId))
                            }
                        }
                    }
                }
            }
        }

    }

    private fun readFromStoreToSong(cursor: Cursor): Song {
        val indexId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val indexName = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val indexDuration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val indexDateAdded = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
        Log.d("MEDIA_LOADER", MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString());

        val id = cursor.getLong(indexId)
        val name = cursor.getString(indexName)
        val duration = cursor.getLong(indexDuration)
        val dateAdded = cursor.getLong(indexDateAdded)

        Log.d("MEDIA_LOADER", id.toString())
        Log.d("MEDIA_LOADER", name)

        val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

        val imageUri = copyThumbnailToInternal(contentUri, id)

        val song = Song(
            songId = id,
            name = name,
            songUri = contentUri.toString(),
            duration = duration,
            dateAdded = dateAdded,
            imageUri = imageUri?.toString()
        )
        return song
    }

    fun copyThumbnailToInternal(contentUri: Uri, id: Long): Uri? {
        try {
            val thumbnail: Bitmap = context.contentResolver.loadThumbnail(
                contentUri,
                Size(800, 800),
                null
            )
            val thumbnailPath = "images/$id.png"
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