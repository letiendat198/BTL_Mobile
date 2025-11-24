package com.ptit.btl_mobile.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.SongWithArtists
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    var recentlyAddedSongs by mutableStateOf<List<SongWithArtists>>(emptyList())
        private set

    init {
        loadRecentlyAddedSongs(20)
    }

    /**
     * Tải danh sách các bài hát được thêm gần đây nhất từ cơ sở dữ liệu.
     * @param limit Số lượng bài hát cần lấy.
     */
    private fun loadRecentlyAddedSongs(limit: Int) {
        viewModelScope.launch {
            val db = Database.getInstance()
            recentlyAddedSongs = db.SongDAO().getRecentlyAdded(limit)
        }
    }
}