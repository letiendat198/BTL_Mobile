package com.ptit.btl_mobile.ui.screens.player

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.ptit.btl_mobile.model.ai.RecommendationEngine
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.SongWithArtists
import com.ptit.btl_mobile.model.media.MediaLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AltComponent {
    QUEUE,
    SUGGEST,
    LYRIC,
    NONE
}

    // Keep these variables as MutableState.
    // DON'T USE "BY" OR COMPOSE WON'T UPDATE CORRECTLY AS IT WILL SEE IT AS JUST A NORMAL VALUE
    // YOU CAN USE "BY" IN COMPOSE NORMALLY
class PlayerViewModel(application: Application): AndroidViewModel(application) {
    var currentSong = mutableStateOf<SongWithArtists?>(null)
    private var _currentQueue = listOf<SongWithArtists>()
    val currentQueue = mutableStateOf(_currentQueue)
    var currentPosition = mutableLongStateOf(0) // Playback position in seconds
    var collectPositionJob: Job? = null

    var showAltComponent by mutableStateOf(false)
    var currentAltComponent by mutableStateOf<AltComponent>(AltComponent.NONE)

    // --- TÍCH HỢP AI GỢI Ý ---
    private val recommendationEngine = RecommendationEngine(application.applicationContext)
    private var _allSongsForRecommendation = listOf<SongWithArtists>()
    val _recommendedSongs = mutableStateListOf<SongWithArtists>()
    val recommendedSongs: List<SongWithArtists> = _recommendedSongs
    // -------------------------

    var currentSongIndex = -1
        private set(value) {
            val newSong = _currentQueue.getOrNull(value)
            if (field != value || currentSong.value?.song?.songId != newSong?.song?.songId) {
                field = value
                currentSong.value = newSong
                // Khi bài hát thay đổi, cập nhật danh sách gợi ý
                newSong?.let { updateRecommendations(it) }
            }
        }

    var mediaController: MediaController? = null
        set(value) {
            if (value!=null) {
                field = value
                value.addListener(MediaControlCallback())
            }
        }

    init {
        // Tải trước tất cả bài hát để phục vụ cho việc gợi ý
        viewModelScope.launch(Dispatchers.IO) {
            _allSongsForRecommendation = Database.getInstance().SongDAO().getAllWithArtists()
        }
    }

    inner class MediaControlCallback: Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            collectPositionJob?.cancel()
            if (isPlaying) {
                collectPositionJob = viewModelScope.launch {
                    while (true) {
                        currentPosition.longValue = (mediaController?.currentPosition ?: 0) / 1000
                        delay(1000)
                    }
                }
            } else {
                collectPositionJob = null
            }
        }

        override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex) {
                currentSongIndex = newPosition.mediaItemIndex
            // No need to update seekbar from here. Coroutine will update it anyways
            }
        }

        // Fire if song move on naturally or seekTo
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)

            // Media controller should never be null at this point
            currentSongIndex = mediaController?.currentMediaItemIndex!!
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Log.d("PLAYER", "Playback failed: ${error.errorCodeName}")
            mediaController?.seekToNextMediaItem()
            // Theory: Maybe this change player state so that it's not stuck in an erroneous state
            // And keep skipping songs and repeating this callback many times
            // Calling .prepare() consistently prevent the bug
            mediaController?.prepare()
            mediaController?.play()

            if (error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
                Toast.makeText(application, "Can't play! Skipping", Toast.LENGTH_SHORT).show()
                // Problematic, will trigger clean up twice or more, causing crash
                // Because 2 coroutine start at the same time will get the song list with current song still on it
                // and try to delete it => Race
//                val currentSongId = currentSong.value?.song?.songId
//                updateCurrentQueue(currentQueue.value.filter { (song, artist) ->
//                    song.songId != currentSongId // Here we assume that current song ID is what cause the problem
//                })
                val mediaLoader = MediaLoader(application, viewModelScope)
                mediaLoader.cleanUpSong() // Maybe not a good idea
            }
            else {
                Toast.makeText(application, error.errorCodeName, Toast.LENGTH_SHORT).show()
            }
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
        if (queue === _currentQueue) {
            Log.d("PLAYER", "New queue and current queue is the same list ref")
            return
        }

        _currentQueue = queue
        currentQueue.value = _currentQueue

        mediaController?.clearMediaItems()
        mediaController?.setMediaItems(getPlaylistFromQueue())
    }

    private fun getPlaylistFromQueue(): List<MediaItem> {
        return _currentQueue.map { MediaItem.fromUri(it.song.songUri) }
    }

    // --- HÀM GỢI Ý MỚI ---
    private fun updateRecommendations(seedSong: SongWithArtists) {
        viewModelScope.launch(Dispatchers.IO) {
            // Gọi recommendation engine để lấy danh sách Song
            val recommendedRawSongs = recommendationEngine.getRecommendations(
                seedSong = seedSong,
                allSongs = _allSongsForRecommendation,
                limit = 15
            )
            // Chuyển đổi từ List<Song> sang List<SongWithArtists>
            val recommendedSongsWithArtists = recommendedRawSongs.mapNotNull { song ->
                _allSongsForRecommendation.find { it.song.songId == song.songId }
            }

            withContext(Dispatchers.Main) {
                _recommendedSongs.clear()
                _recommendedSongs.addAll(recommendedSongsWithArtists)
                Log.d("AI_ENGINE", "Updated recommendations: ${_recommendedSongs.size} songs")
            }
        }
    }
}