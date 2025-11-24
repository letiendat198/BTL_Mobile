package com.ptit.btl_mobile.ui.screens.player

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.ptit.btl_mobile.model.ai.RecommendationEngine
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.Song
import com.ptit.btl_mobile.model.database.SongWithArtists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerViewModel(application: Application): AndroidViewModel(application) {
    var currentSong = mutableStateOf<SongWithArtists?>(null)
    private var _currentQueue = listOf<SongWithArtists>()
    val currentQueue = mutableStateOf(_currentQueue)
    var currentPosition = mutableLongStateOf(0)
    var collectPositionJob: Job? = null

    var showQueue by mutableStateOf(false)
    var showRecommendations by mutableStateOf(true) // State để ẩn/hiện gợi ý

    // --- TÍCH HỢP AI GỢI Ý ---
    private val recommendationEngine = RecommendationEngine(application.applicationContext)
    private var _allSongsForRecommendation = listOf<SongWithArtists>()
    private val _recommendedSongs = mutableStateListOf<SongWithArtists>()
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
            if (field != value) {
                field = value
                value?.addListener(MediaControlCallback())
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
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            mediaController?.currentMediaItemIndex?.let { currentSongIndex = it }
        }
    }

    fun playSong(index: Int, queue: List<SongWithArtists>) {
        updateCurrentQueue(queue)
        mediaController?.seekTo(index, 0)
        if (mediaController?.isPlaying == false) {
            mediaController?.prepare()
            mediaController?.play()
        }
        // Cập nhật index và bài hát ngay lập tức
        currentSongIndex = index
    }

    private fun updateCurrentQueue(queue: List<SongWithArtists>) {
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