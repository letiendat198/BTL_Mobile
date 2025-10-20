package com.ptit.btl_mobile.ui.screens.playlist

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.Playlist
import kotlinx.coroutines.launch

class PlaylistViewModel : ViewModel() {
    private var _playlists = listOf<Playlist>()
    var playlists = mutableStateOf(_playlists)

    init {
        Log.d("PLAYLIST_VIEW_MODEL", "ViewModel created")
        getAllPlaylists()
    }

    fun getAllPlaylists() {
        val db = Database.getInstance()
        viewModelScope.launch {
            _playlists = db.PlaylistDAO().getAll()
            playlists.value = _playlists
        }
    }
}
