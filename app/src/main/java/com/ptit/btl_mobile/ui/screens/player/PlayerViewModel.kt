package com.ptit.btl_mobile.ui.screens.player

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.ptit.btl_mobile.model.database.SongWithArtists
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel: ViewModel() {
    var currentSong = mutableStateOf<SongWithArtists?>(null)
    private var _currentQueue = listOf<SongWithArtists>()
    val currentQueue = mutableStateOf(_currentQueue)
    var currentPosition = mutableLongStateOf(0) // Playback position in seconds
    var collectPositionJob: Job? = null

    // Index is not usually need, but may come in handy when selecting an item in list in compose
    var currentSongIndex = -1
        get() = field
        set(value) {
            currentSong.value = _currentQueue.getOrNull(value)
            field = value
        }

    var mediaController: MediaController? = null
        get() = field
        set(value) {
            if (value!=null) {
                value.addListener(MediaControlCallback())
            }
            field = value
        }

    inner class MediaControlCallback: Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)

            if (isPlaying) {
                collectPositionJob?.cancel()
                collectPositionJob = viewModelScope.launch {
                    while (true) {
                        currentPosition.longValue = (mediaController?.currentPosition ?: 0) / 1000
                        delay(1000)
                    }
                }
            }
            else {
                collectPositionJob?.cancel()
                collectPositionJob = null
            }
        }

        // Fire if use seekTo
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)

            if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex)
                currentSongIndex = newPosition.mediaItemIndex
            else {
                // TODO: Seekbar
            }
        }

        // Fire if song move on naturally or seekTo
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)

            // Media controller should never be null at this point
            currentSongIndex = mediaController?.currentMediaItemIndex!!
        }
    }

    fun playSong(index: Int, queue: List<SongWithArtists>) {
        updateCurrentQueue(queue)
        currentSongIndex = index
        mediaController?.seekTo(index, 0)

        mediaController?.prepare()
        mediaController?.play()
    }

    private fun updateCurrentQueue(queue: List<SongWithArtists>) {
        // Should only update if song list is different. Only comparing references for speed
        if (queue === _currentQueue) return

        _currentQueue = queue
        currentQueue.value = _currentQueue

        mediaController?.clearMediaItems()
        mediaController?.setMediaItems(getPlaylistFromQueue())
    }

    private fun getPlaylistFromQueue(): List<MediaItem> {
        val mediaList = mutableListOf<MediaItem>()

        _currentQueue.forEach { (song, artists) ->
            mediaList.add(MediaItem.fromUri(song.songUri))
        }

        return mediaList
    }
}