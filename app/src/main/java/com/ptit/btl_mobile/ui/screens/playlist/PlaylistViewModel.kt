package com.ptit.btl_mobile.ui.screens.playlist

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.Playlist
import com.ptit.btl_mobile.model.database.PlaylistSongCrossRef
import com.ptit.btl_mobile.model.database.SongWithArtists
import com.ptit.btl_mobile.model.media.PlaylistImageHelper
import com.ptit.btl_mobile.ui.screens.home.autoGeneratePlaylist.AutoPlaylistGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class PlaylistDraft(
    val name: String = "",
    val imageUri: String? = null,
    val tempImageUri: Uri? = null,  // URI tạm từ image picker
    val selectedSongIds: List<Long> = emptyList()
)

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {
    // View model instance is stored statically inside a view model store owner
    // So this attribute will keep a reference to context and won't let it be clean up
    // This may potentially leak memory for activity context (because activity get destroyed all the time)
    // But, it shouldn't matter to application context, which lives for the whole process
    // Comment it out anyways to avoid warnings - Dat
//    private val context = application.applicationContext
    private val db = Database.getInstance()
    private val playlistDao = db.PlaylistDAO()
    private val songDao = db.SongDAO()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists = _playlists.asStateFlow()

    private val _allSongs = MutableStateFlow<List<SongWithArtists>>(emptyList())
    val allSongs = _allSongs.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist = _selectedPlaylist.asStateFlow()

    private val _playlistDraft = MutableStateFlow(PlaylistDraft())
    val playlistDraft = _playlistDraft.asStateFlow()

    val selectedSongIds = mutableStateListOf<Long>()

    private val _playlistSongs = MutableStateFlow<List<SongWithArtists>>(emptyList())
    val playlistSongs = _playlistSongs.asStateFlow()

    init {
        getAllPlaylists()
        loadAllSongs()
    }

    fun loadSongsForPlaylist(playlistId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _playlistSongs.value = songDao.getSongsByPlaylistId(playlistId)
        }
    }

    private fun loadAllSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            _allSongs.value = songDao.getAllWithArtists()
        }
    }

    private fun getAllPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            _playlists.value = playlistDao.getAll()
        }
    }

    fun selectPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            _selectedPlaylist.emit(playlist)
        }
    }

    /**
     * Bắt đầu tạo playlist mới - reset toàn bộ draft
     */
    fun startCreatingPlaylist() {
        _playlistDraft.value = PlaylistDraft()
        selectedSongIds.clear()
        Log.d("PlaylistViewModel", "Started creating new playlist")
    }

    /**
     * Xóa draft (khi user bấm back)
     */
    fun clearDraft() {
        _playlistDraft.value = PlaylistDraft()
        selectedSongIds.clear()
        Log.d("PlaylistViewModel", "Cleared draft")
    }

    /**
     * Cập nhật tên playlist trong draft
     */
    fun updateDraftName(name: String) {
        _playlistDraft.value = _playlistDraft.value.copy(name = name)
        Log.d("PlaylistViewModel", "Updated draft name: $name")
    }

    /**
     * Cập nhật ảnh trong draft
     */
    fun updateDraftImage(imageUri: String?) {
        _playlistDraft.value = _playlistDraft.value.copy(imageUri = imageUri)
        Log.d("PlaylistViewModel", "Updated draft image: $imageUri")
    }

    /**
     * Lưu thông tin cơ bản của playlist (tên, ảnh)
     * Gọi khi chuyển từ CreatePlaylistScreen sang SelectSongsScreen
     */
    fun saveDraftInfo(name: String, imageUri: String?, tempImageUri: Uri? = null) {
        _playlistDraft.value = _playlistDraft.value.copy(
            name = name,
            imageUri = imageUri,
            tempImageUri = tempImageUri
        )
        Log.d("PlaylistViewModel", "Saved draft info - Name: $name, Image: $imageUri, TempUri: $tempImageUri")
    }

    /**
     * Cập nhật danh sách bài hát đã chọn
     * Gọi từ SelectSongsScreen
     */
    fun updateDraftSelectedSongs(songIds: List<Long>) {
        selectedSongIds.clear()
        selectedSongIds.addAll(songIds)
        _playlistDraft.value = _playlistDraft.value.copy(selectedSongIds = songIds)
        Log.d("PlaylistViewModel", "Updated draft songs: ${songIds.size} songs")
    }

    /**
     * Xác nhận tạo playlist - lưu vào database
     * Gọi khi user bấm "Done" ở SelectSongsScreen
     */
    fun confirmCreatePlaylist() {
        viewModelScope.launch(Dispatchers.IO) {
            val draft = _playlistDraft.value

            if (draft.name.isBlank()) {
                Log.e("PlaylistViewModel", "Cannot create playlist: name is blank")
                return@launch
            }

            try {
                // Bước 1: Tạo playlist tạm với imageUri = null
                val tempPlaylist = Playlist(
                    name = draft.name,
                    imageUri = null,
                    dateCreated = Date()
                )
                val playlistId = playlistDao.insertPlaylist(tempPlaylist)
                Log.d("PlaylistViewModel", "Created playlist with ID: $playlistId")

                // Bước 2: Xử lý ảnh
                var finalImageUri: String? = null

                if (draft.tempImageUri != null) {
                    // Có ảnh custom - copy vào internal storage
                    Log.d("PlaylistViewModel", "Copying custom image to internal storage...")
                    val savedUri = PlaylistImageHelper.copyImageToInternal(
                        context = application.applicationContext,
                        sourceUri = draft.tempImageUri,
                        playlistId = playlistId
                    )
                    finalImageUri = savedUri?.toString()
                    Log.d("PlaylistViewModel", "Custom image saved: $finalImageUri")
                } else if (draft.selectedSongIds.isNotEmpty()) {
                    // Không có ảnh custom - dùng ảnh bài hát đầu tiên
                    finalImageUri = _allSongs.value
                        .find { it.song.songId == draft.selectedSongIds.firstOrNull() }
                        ?.song?.imageUri
                    Log.d("PlaylistViewModel", "Using first song image: $finalImageUri")
                }

                // Bước 3: Update playlist với ảnh cuối cùng
                if (finalImageUri != null) {
                    val updatedPlaylist = tempPlaylist.copy(
                        playlistId = playlistId,
                        imageUri = finalImageUri
                    )
                    playlistDao.update(updatedPlaylist)
                    Log.d("PlaylistViewModel", "Updated playlist with image")
                }

                // Bước 4: Thêm bài hát vào playlist
                if (draft.selectedSongIds.isNotEmpty()) {
                    val crossRefs = draft.selectedSongIds.map { songId ->
                        PlaylistSongCrossRef(playlistId = playlistId, songId = songId)
                    }
                    playlistDao.addSongsToPlaylist(crossRefs)
                    Log.d("PlaylistViewModel", "Added ${crossRefs.size} songs to playlist")
                }

                // Bước 5: Reset draft và reload playlists
                startCreatingPlaylist()
                getAllPlaylists()

                Log.d("PlaylistViewModel", "Playlist created successfully!")
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error creating playlist", e)
            }
        }
    }

    /**
     * Toggle chọn/bỏ chọn bài hát
     */
    fun toggleSongSelection(songId: Long) {
        if (selectedSongIds.contains(songId)) {
            selectedSongIds.remove(songId)
            Log.d("PlaylistViewModel", "Deselected song: $songId")
        } else {
            selectedSongIds.add(songId)
            Log.d("PlaylistViewModel", "Selected song: $songId")
        }
    }

    /**
     * Xóa toàn bộ selection
     */
    fun clearSongSelection() {
        selectedSongIds.clear()
        Log.d("PlaylistViewModel", "Cleared song selection")
    }

    /**
     * Cập nhật tên playlist
     */
    fun updatePlaylistName(playlist: Playlist, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedPlaylist = playlist.copy(name = newName)
                playlistDao.update(updatedPlaylist)
                getAllPlaylists()

                // Nếu đang xem chi tiết playlist này, cập nhật luôn
                if (_selectedPlaylist.value?.playlistId == playlist.playlistId) {
                    _selectedPlaylist.value = updatedPlaylist
                }

                Log.d("PlaylistViewModel", "Updated playlist name to: $newName")
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error updating playlist name", e)
            }
        }
    }

    /**
     * Xóa playlist
     */
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Xóa ảnh custom nếu có
                PlaylistImageHelper.deletePlaylistImage(application.applicationContext, playlist.playlistId)
                Log.d("PlaylistViewModel", "Deleted playlist image")

                // Xóa các bài hát trong playlist
                playlistDao.clearPlaylist(playlist.playlistId)

                // Xóa playlist
                playlistDao.delete(playlist)

                // Reload danh sách
                getAllPlaylists()

                Log.d("PlaylistViewModel", "Deleted playlist: ${playlist.name}")
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error deleting playlist", e)
            }
        }
    }

    /**
     * Xóa một bài hát khỏi playlist
     */
    fun removeSongFromPlaylist(playlist: Playlist, song: SongWithArtists) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                playlistDao.deleteSongFromPlaylist(playlist.playlistId, song.song.songId)
                // Cập nhật lại danh sách bài hát của playlist đang xem
                val updatedSongs = _playlistSongs.value.filterNot { it.song.songId == song.song.songId }
                _playlistSongs.value = updatedSongs

                Log.d("PlaylistViewModel", "Removed song ${song.song.songId} from playlist ${playlist.playlistId}")
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error removing song from playlist", e)
            }
        }
    }

    /**
     * Thêm các bài hát đã chọn vào một playlist đã có
     */
    fun addSongsToExistingPlaylist(playlistId: Long, songIds: List<Long>) {
        if (songIds.isEmpty()) {
            Log.d("PlaylistViewModel", "No songs selected to add.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val crossRefs = songIds.map { songId ->
                    PlaylistSongCrossRef(playlistId = playlistId, songId = songId)
                }
                playlistDao.addSongsToPlaylist(crossRefs)
                // Reload songs for the current playlist to reflect changes
                if (_selectedPlaylist.value?.playlistId == playlistId) {
                    loadSongsForPlaylist(playlistId)
                }
                Log.d("PlaylistViewModel", "Added ${crossRefs.size} songs to playlist $playlistId")
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error adding songs to existing playlist", e)
            }
        }
    }


    fun createAutoGeneratedPlaylist(
        name: String,
        songs: List<SongWithArtists>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (name.isBlank() || songs.isEmpty()) {
                    Log.e("PlaylistViewModel", "Cannot create playlist: invalid data")
                    return@launch
                }

                // Lấy ảnh từ bài hát đầu tiên
                val imageUri = songs.firstOrNull()?.song?.imageUri

                // Tạo playlist
                val playlist = Playlist(
                    name = name,
                    imageUri = imageUri,
                    dateCreated = Date()
                )
                val playlistId = playlistDao.insertPlaylist(playlist)
                Log.d("PlaylistViewModel", "Created auto-generated playlist with ID: $playlistId")

                // Thêm bài hát vào playlist
                val crossRefs = songs.map { song ->
                    PlaylistSongCrossRef(playlistId = playlistId, songId = song.song.songId)
                }
                playlistDao.addSongsToPlaylist(crossRefs)
                Log.d("PlaylistViewModel", "Added ${crossRefs.size} songs to playlist")

                // Reload playlists
                getAllPlaylists()

                Log.d("PlaylistViewModel", "Auto-generated playlist created successfully!")
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error creating auto-generated playlist", e)
            }
        }
    }

    /**
     * MẤY HÀM TEST: THÍCH DÙNG THÌ THÊM VÀO
     * Generate playlist by artist name
     */
    fun generatePlaylistByArtist(artistName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val songs = AutoPlaylistGenerator.generateByArtist(_allSongs.value, artistName)

                if (songs.isNotEmpty()) {
                    val playlistName = "Best of $artistName"
                    createAutoGeneratedPlaylist(playlistName, songs)
                } else {
                    Log.w("PlaylistViewModel", "No songs found for artist: $artistName")
                }
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error generating playlist by artist", e)
            }
        }
    }

    /**
     * Generate playlist by first letter. Thích dùng thì thêm
     */
    fun generatePlaylistByLetter(letter: Char) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val songs = AutoPlaylistGenerator.generateByFirstLetter(_allSongs.value, letter)

                if (songs.isNotEmpty()) {
                    val playlistName = "Songs starting with $letter"
                    createAutoGeneratedPlaylist(playlistName, songs)
                } else {
                    Log.w("PlaylistViewModel", "No songs found for letter: $letter")
                }
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error generating playlist by letter", e)
            }
        }
    }
}