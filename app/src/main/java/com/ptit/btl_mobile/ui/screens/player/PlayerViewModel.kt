package com.ptit.btl_mobile.ui.screens.player

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.ptit.btl_mobile.model.database.SongWithArtists

class PlayerViewModel: ViewModel() {
    private var currentSongIndex = mutableIntStateOf(-1)
    private var _currentQueue = listOf<SongWithArtists>()
    val currentQueue = mutableStateOf(_currentQueue)

    var mediaController: MediaController? = null
        get() = field
        set(value) {
            if (value!=null) {
                value.addListener(MediaControlCallback())
            }
            field = value
        }

    inner class MediaControlCallback: Player.Listener {
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)

            if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex)
                currentSongIndex.intValue = oldPosition.mediaItemIndex
            // TODO: Update position in seekbar
        }

        // TODO: BUGGY VIEW UPDATE!!!
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)

            // Media controller should never be null at this point
            currentSongIndex.intValue = mediaController?.currentMediaItemIndex!!
        }
    }

    fun playSong(index: Int, queue: List<SongWithArtists>) {
        updateCurrentQueue(queue)
        currentSongIndex.intValue = index
        mediaController?.seekTo(index, 0)

        mediaController?.prepare()
        mediaController?.play()
    }

    fun getCurrentSongIndexState(): MutableIntState {
        return currentSongIndex
    }

    fun getCurrentSong(): SongWithArtists? {
        if (currentSongIndex.intValue > -1) return currentQueue.value[currentSongIndex.intValue]
        else return null
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