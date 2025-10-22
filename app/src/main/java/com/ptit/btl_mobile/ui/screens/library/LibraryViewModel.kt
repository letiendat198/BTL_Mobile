package com.ptit.btl_mobile.ui.screens.library

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.SongWithArtists
import kotlinx.coroutines.launch

class LibraryViewModel: ViewModel() {
    var _songs = listOf<SongWithArtists>()
    var songs = mutableStateOf(_songs)

    // This will run everytime the composable is create or re-created (navigated in or out)
    init {
        Log.d("LIBRARY_VIEW_MODEL", "ViewModel is being constructed. This should only happens once")
        getAllSongs()
    }

    fun getAllSongs() {
        val db = Database.getInstance()
        // This may block Main thread if any function called inside is not suspend function
        // Wrap blocking function in withContext and dispatch to IO
        viewModelScope.launch {
            _songs = db.SongDAO().getAllWithArtists()
            songs.value = _songs // Actually update the state with the new list. This will cause a recompose
        }
    }
}