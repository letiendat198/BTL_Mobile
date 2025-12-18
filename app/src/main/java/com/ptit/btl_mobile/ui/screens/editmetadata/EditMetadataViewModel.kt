package com.ptit.btl_mobile.ui.screens.editmetadata

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.btl_mobile.model.database.Album
import com.ptit.btl_mobile.model.database.Artist
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

data class SongWithInfo(var song: Song, var artists: List<Artist>, var album: Album)

class EditMetadataViewModel: ViewModel() {

    fun getSongFullInfo(songId: Long): Flow<SongWithInfo> {
        val db = Database.getInstance()
        return flow {
            val songWithAlbum = db.SongDAO().getSongWithAlbum(songId)
            val songWithArtists = db.SongDAO().getSongWithArtists(songId)

            val songWithInfo = SongWithInfo(
                songWithArtists.song,
                songWithArtists.artists,
                songWithAlbum.album?: Album(albumId = -1, name = ""))

            emit(songWithInfo)
        }
    }

    fun saveSongMetadata(songInfo: SongWithInfo) {
        Log.d("EDIT_METADATA_VM", String.format("Song name: %s, artist: %s, album: %s, genre: %s, albumImage: %s",
            songInfo.song.name,
            songInfo.artists.joinToString(", ") { it.name },
            songInfo.album.name,
            songInfo.album.genre,
            songInfo.album.imageUri))
        val db = Database.getInstance()
        viewModelScope.launch(Dispatchers.IO) {
            db.SongDAO().updateSongName(songInfo.song.songId, songInfo.song.name)
            if (!songInfo.song.imageUri.isNullOrEmpty()) {
                db.SongDAO().updateSongImage(songInfo.song.songId, songInfo.song.imageUri!!)
            }
//            val albumId = db.AlbumDAO().safeInsertAlbum(songInfo.album)
//            db.SongDAO().updateSongAlbum(songInfo.song.songId, albumId)
        }
    }
}