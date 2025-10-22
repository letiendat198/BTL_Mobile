package com.ptit.btl_mobile.ui.screens.player

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ptit.btl_mobile.model.database.SongWithArtists

class PlayerViewModel: ViewModel() {
    var currentSong = mutableStateOf<SongWithArtists?>(null)
    var _currentQueue = listOf<SongWithArtists>()
    val currentQueue = mutableStateOf(_currentQueue)

    fun updateCurrentQueue(queue: List<SongWithArtists>) {
        _currentQueue = queue
        currentQueue.value = _currentQueue
    }
}