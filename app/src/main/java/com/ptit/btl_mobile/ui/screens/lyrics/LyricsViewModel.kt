package com.ptit.btl_mobile.ui.screens.lyrics

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.lyrics.LrcLine
import com.ptit.btl_mobile.model.lyrics.LrcParser
import com.ptit.btl_mobile.model.lyrics.LyricsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LyricsViewModel(context: Context, private val songId: Long) : ViewModel() {

    private val lyricsManager = LyricsManager(context)
    private val db = Database.getInstance()

    private val _lyrics = MutableStateFlow<String?>(null)
    val lyrics: StateFlow<String?> = _lyrics.asStateFlow()

    private val _lrcLines = MutableStateFlow<List<LrcLine>>(emptyList())
    val lrcLines: StateFlow<List<LrcLine>> = _lrcLines.asStateFlow()

    private val _currentLineIndex = MutableStateFlow(0)
    val currentLineIndex: StateFlow<Int> = _currentLineIndex.asStateFlow()

    private val _isSynced = MutableStateFlow(false)
    val isSynced: StateFlow<Boolean> = _isSynced.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadLyrics()
    }

    private fun loadLyrics() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true

            val song = db.SongDAO().getSongById(songId)
            val lyricUri = song?.lyricUri

            val content = lyricsManager.getLyricsFromUri(lyricUri)
            _lyrics.value = content

            if (content != null && content.contains(Regex("""\[\d{2}:\d{2}\.\d{2}\]"""))) {
                _isSynced.value = true
                _lrcLines.value = LrcParser.parse(content)
            } else {
                _isSynced.value = false
            }

            _isLoading.value = false
        }
    }

    fun updateCurrentPosition(positionMs: Long) {
        if (!_isSynced.value) return

        val lines = _lrcLines.value
        if (lines.isEmpty()) return

        var index = 0
        for (i in lines.indices) {
            if (positionMs >= lines[i].timeInMillis) {
                index = i
            } else {
                break
            }
        }
        _currentLineIndex.value = index
    }

    fun saveLyrics(content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val lyricUri = lyricsManager.saveLyricsToFile(songId, content)

            val song = db.SongDAO().getSongById(songId)
            song?.let {
                val updatedSong = it.copy(lyricUri = lyricUri)
                db.SongDAO().update(updatedSong)
            }

            _lyrics.value = content
            _isEditing.value = false
            loadLyrics()
        }
    }

    fun deleteLyrics() {
        viewModelScope.launch(Dispatchers.IO) {
            val song = db.SongDAO().getSongById(songId)
            song?.let {
                lyricsManager.deleteLyricsFile(it.lyricUri)

                val updatedSong = it.copy(lyricUri = null)
                db.SongDAO().update(updatedSong)
            }

            _lyrics.value = null
            _lrcLines.value = emptyList()
            _isSynced.value = false
        }
    }

    fun importLrcFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true

            val lyricUri = lyricsManager.importLrcFile(songId, uri)

            val song = db.SongDAO().getSongById(songId)
            song?.let {
                val updatedSong = it.copy(lyricUri = lyricUri)
                db.SongDAO().update(updatedSong)
            }

            loadLyrics()
            _isLoading.value = false
        }
    }

    fun startEditing() {
        _isEditing.value = true
    }

    fun cancelEditing() {
        _isEditing.value = false
    }
}