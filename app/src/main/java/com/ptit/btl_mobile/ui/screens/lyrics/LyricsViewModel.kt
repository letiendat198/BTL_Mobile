package com.ptit.btl_mobile.ui.screens.lyrics

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            val content = lyricsManager.getLyrics(songId)
            _lyrics.value = content

            // Check if synced (có timestamp)
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

        // Tìm dòng hiện tại dựa vào thời gian
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
            lyricsManager.saveLyrics(songId, content)
            _lyrics.value = content
            _isEditing.value = false
            loadLyrics()
        }
    }

    fun deleteLyrics() {
        viewModelScope.launch(Dispatchers.IO) {
            lyricsManager.deleteLyrics(songId)
            _lyrics.value = null
            _lrcLines.value = emptyList()
            _isSynced.value = false
        }
    }

    fun importLrcFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val content = lyricsManager.importFromLrcFile(songId, uri)
            _lyrics.value = content
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