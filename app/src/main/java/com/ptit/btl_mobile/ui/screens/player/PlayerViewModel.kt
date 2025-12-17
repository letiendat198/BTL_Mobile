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
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.SongWithArtists
import com.ptit.btl_mobile.model.media.MediaLoader
import com.ptit.btl_mobile.recommendation.RecommenderEngine
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

class PlayerViewModel(application: Application): AndroidViewModel(application) {
    var currentSong = mutableStateOf<SongWithArtists?>(null)
    private var _currentQueue = listOf<SongWithArtists>()
    val currentQueue = mutableStateOf(_currentQueue)
    var currentPosition = mutableLongStateOf(0)
    var collectPositionJob: Job? = null

    var showAltComponent by mutableStateOf(false)
    var currentAltComponent by mutableStateOf<AltComponent>(AltComponent.NONE)

    private var recommendationEngine: RecommenderEngine? = null
    private var _allSongsForRecommendation = listOf<SongWithArtists>()
    val _recommendedSongs = mutableStateListOf<SongWithArtists>()
    val recommendedSongs: List<SongWithArtists> = _recommendedSongs

    var currentSongIndex = -1
        private set(value) {
            val newSong = _currentQueue.getOrNull(value)
            if (field != value || currentSong.value?.song?.songId != newSong?.song?.songId) {
                field = value
                currentSong.value = newSong
                newSong?.let { updateRecommendations(it.song.songId) }
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
        viewModelScope.launch(Dispatchers.IO) {
            val db = Database.getInstance()
            val allSongs = db.SongDAO().getAll()
            val allArtists = db.ArtistDAO().getAll()
            val allAlbums = db.AlbumDAO().getAll()
            val allPlaylistSongCrossRefs = db.PlaylistDAO().getAllPlaylistSongCrossRefs()
            val allSongArtistCrossRefs = db.SongDAO().getAllSongArtistCrossRefs()
            _allSongsForRecommendation = db.SongDAO().getAllWithArtists()

            recommendationEngine = RecommenderEngine(
                songs = allSongs,
                songArtists = allSongArtistCrossRefs,
                albums = allAlbums,
                playlistSongs = allPlaylistSongCrossRefs
            )
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
            currentSongIndex = mediaController?.currentMediaItemIndex!!
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Log.d("PLAYER", "Playback failed: ${error.errorCodeName}")
            mediaController?.seekToNextMediaItem()
            mediaController?.prepare()
            mediaController?.play()

            val app = getApplication<Application>()

            if (error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
                Toast.makeText(app, "Can't play! Skipping", Toast.LENGTH_SHORT).show()
                val mediaLoader = MediaLoader(app, viewModelScope)
                mediaLoader.cleanUpSong()
            }
            else {
                Toast.makeText(app, error.errorCodeName, Toast.LENGTH_SHORT).show()
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

    private fun updateRecommendations(seedSongId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (recommendationEngine == null) {
                Log.d("AI_ENGINE", "Engine not ready")
                return@launch
            }
            val recommendedIds = recommendationEngine!!.recommendSongs(seedSongId, 15)
            val recommendedSongsWithArtists = recommendedIds.mapNotNull { songId ->
                _allSongsForRecommendation.find { it.song.songId == songId }
            }

                withContext(Dispatchers.Main) {
                    _recommendedSongs.clear()
                    _recommendedSongs.addAll(recommendedSongsWithArtists)
                    Log.d("AI_ENGINE", "Updated recommendations: ${_recommendedSongs.size} songs")
                }
            }
            catch (e: Exception) {
                Log.e("AI_ENGINE", e.message?:"Tensorflow crash again")
            }
        }
    }
}