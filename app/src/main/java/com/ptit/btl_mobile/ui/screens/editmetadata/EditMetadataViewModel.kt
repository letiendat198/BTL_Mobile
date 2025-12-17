package com.ptit.btl_mobile.ui.screens.editmetadata

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

    }
}